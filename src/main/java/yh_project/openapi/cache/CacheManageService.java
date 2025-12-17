package yh_project.openapi.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CacheManageService {
    private final CacheManager cacheManager;

    public <T> T get(String cacheName, String key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return null;

        // Spring이 내부 직렬화/역직렬화까지 처리해줌(설정에 따라)
        return cache.get(key, type);
    }

    public void put(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) throw new IllegalArgumentException("Unknown cache: " + cacheName);
        cache.put(key, value);
    }

    public void forget(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) throw new IllegalArgumentException("Unknown cache: " + cacheName);
        cache.evict(key);
    }

}
