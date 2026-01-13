# Cloud init

```text
#cloud-config
package_update: true
package_upgrade: false

packages:
  - ca-certificates
  - curl
  - gnupg
  - lsb-release
  - nginx
  - redis-tools
  - mariadb-client
  - net-tools
  - certbot
  - python3-certbot-nginx

write_files:
  - path: /usr/local/bin/bootstrap.sh
    permissions: "0755"
    content: |
      #!/usr/bin/env bash
      set -euxo pipefail

      export DEBIAN_FRONTEND=noninteractive

      # Remove old docker packages (ignore errors)
      apt-get remove -y docker docker-engine docker.io containerd runc || true

      # Docker official repo + GPG
      install -m 0755 -d /etc/apt/keyrings
      curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
      chmod a+r /etc/apt/keyrings/docker.gpg

      source /etc/os-release
      ARCH="$(dpkg --print-architecture)"
      echo "deb [arch=${ARCH} signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu ${VERSION_CODENAME} stable" \
        > /etc/apt/sources.list.d/docker.list

      apt-get update

      # Install Docker
      apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

      # Install Java (try 21, fallback to 17)
      apt-get install -y openjdk-21-jdk || apt-get install -y openjdk-17-jdk

      # Enable/start services
      systemctl enable --now docker
      systemctl enable --now nginx

      # Print versions
      docker --version
      docker compose version || true
      nginx -v || true
      java -version || true

runcmd:
  - [ bash, -lc, "/usr/local/bin/bootstrap.sh" ]
  - [ bash, -lc, "usermod -aG docker azureuser || true" ]
  - [ bash, -lc, "docker run --rm hello-world || true" ]

final_message: |
  cloud-init finished: nginx, docker, tools, and certbot installed.

```


# 초기 설정
1. nginx 도메인 설정
``` nginx
server {
    listen 80;
    server_name example.com www.example.com;

    location / {
        return 200 "ok\n";
    }
}

```
활성화/테스트   
```bash
sudo ln -s /etc/nginx/sites-available/myapp /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

2. 인증서 발급
```bash
sudo apt update
sudo apt install -y snapd
sudo snap install core
sudo snap refresh core

# (혹시 예전에 apt로 certbot 깔았으면 충돌 방지로 제거)
sudo apt remove -y certbot

sudo snap install --classic certbot
sudo ln -s /snap/bin/certbot /usr/bin/certbot
certbot --version

sudo certbot --nginx -d example.com -d www.example.com
```

3. 자동 갱신 확인
```bash
# 확인
systemctl list-timers | grep -E 'certbot|letsencrypt|snap.certbot'
# 갱신 테스트 
sudo certbot renew --dry-run
```
4. nginx 설정 변경
```nginx
server {
    listen 443 ssl http2;
    server_name example.com www.example.com;

    ssl_certificate     /etc/letsencrypt/live/example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/example.com/privkey.pem;

    # (Certbot이 생성/관리하는 보안 파라미터 include)
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    location / {
        proxy_pass http://127.0.0.1:8080;  # Spring Boot 등
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 80;
    server_name example.com www.example.com;
    return 301 https://$host$request_uri;
}
```   
# mariadb install
```bash
sudo apt update
sudo apt install -y curl ca-certificates
```
   
```bash
curl -LsS https://r.mariadb.com/downloads/mariadb_repo_setup \
 | sudo bash -s -- --mariadb-server-version="mariadb-12.1.2"

```
   
```bash
sudo apt update
sudo apt install -y mariadb-server mariadb-client mariadb-backup
```


```bash
sudo systemctl enable --now mariadb
sudo systemctl status mariadb
```
   
