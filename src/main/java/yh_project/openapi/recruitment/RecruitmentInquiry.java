package yh_project.openapi.recruitment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "recruitment_inquiry")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String company;
    private String position;
    private String phone;
    private String message;

    private LocalDateTime createdAt;

    @Builder
    public RecruitmentInquiry(String name, String email, String company, String position, String phone, String message) {
        this.name = name;
        this.email = email;
        this.company = company;
        this.position = position;
        this.phone = phone;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}

