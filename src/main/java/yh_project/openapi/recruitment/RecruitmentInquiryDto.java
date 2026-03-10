package yh_project.openapi.recruitment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentInquiryDto {
    private String name;
    private String email;
    private String company;
    private String position;
    private String phone;
    private String message;

    public RecruitmentInquiry toEntity() {
        return RecruitmentInquiry.builder()
                .name(name)
                .email(email)
                .company(company)
                .position(position)
                .phone(phone)
                .message(message)
                .build();
    }
}

