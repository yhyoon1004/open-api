package yh_project.openapi.recruitment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class RecruitmentInquiryController {

    private final RecruitmentInquiryService recruitmentInquiryService;

    @PostMapping(value = "/", consumes = "application/json")
    public ResponseEntity<Long> save(@RequestBody RecruitmentInquiryDto requestDto) {
        return ResponseEntity.ok(recruitmentInquiryService.save(requestDto));
    }
}

