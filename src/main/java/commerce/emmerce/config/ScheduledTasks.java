package commerce.emmerce.config;

import commerce.emmerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ScheduledTasks {

    private final ProductService productService;

    @Scheduled(cron = "0 0 0 * * *")    // 매일 자정 실행
    public void updateProductStarScore() {
        productService.updateAllProductStarScore().block();
    }

}
