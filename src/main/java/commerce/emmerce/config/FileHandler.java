package commerce.emmerce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FileHandler {

    public Mono<String> saveImage(Mono<FilePart> imagePart, String dirPath) {
        return imagePart.flatMap(img -> {
            String uniqueFileName = UUID.randomUUID() + img.filename();
            return img.transferTo(Paths.get(dirPath + uniqueFileName))
                    .thenReturn(uniqueFileName);
        });
    }

    public Mono<List<String>> savedImagesAndGetPaths(Flux<FilePart> images, String dirPath) {
        return images.flatMap(image -> saveImage(Mono.just(image), dirPath))
                .map(imageName -> dirPath + imageName)
                .collectList();
    }

    public Mono<Void> deleteImage(String imagePath) {
        return Mono.fromCallable(() -> {
            Path path = Paths.get(imagePath);
            return Files.deleteIfExists(path);
        }).onErrorResume(e -> {
            log.error(imagePath + " 파일 삭제에 실패했습니다.", e);
            return Mono.empty();
        }).then();
    }

    public Flux<Void> deleteImages(List<String> imagePaths) {
        return Flux.fromIterable(imagePaths)
                .flatMap(this::deleteImage);
    }

}
