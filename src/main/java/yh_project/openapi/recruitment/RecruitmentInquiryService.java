package yh_project.openapi.recruitment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentInquiryService {

    private final RecruitmentInquiryRepository recruitmentInquiryRepository;

    @Transactional
    public Long save(RecruitmentInquiryDto dto) {
        return recruitmentInquiryRepository.save(dto.toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public List<RecruitmentInquiry> findAll() {
        return recruitmentInquiryRepository.findAll();
    }
}

