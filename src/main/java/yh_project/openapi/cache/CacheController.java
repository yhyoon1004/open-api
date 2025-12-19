package yh_project.openapi.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yh_project.openapi.weather.dto.WeatherItemDTO;

import java.util.List;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheController {
    private final CacheManageService cacheManageService;

    @RequestMapping("/get/{key}")
    public ResponseEntity<?> getCache(@PathVariable String key){
        List<WeatherItemDTO> data = cacheManageService.get(key, "", List.class);
        return ResponseEntity.ok().body(data);
    }

}
