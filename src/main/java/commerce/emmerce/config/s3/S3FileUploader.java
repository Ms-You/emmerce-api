package commerce.emmerce.config.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3FileUploader {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private final S3AsyncClient s3AsyncClient;

    /**
     * 이미지 목록 S3 bucket 에 업로드
     * @param imageFiles
     * @param dirName
     * @return
     */
    public Mono<List<String>> uploadS3ImageList(Flux<FilePart> imageFiles, String dirName) {
        return imageFiles.flatMap(imageFile -> uploadS3Image(Mono.just(imageFile), dirName))
                .collectList();
    }

    /**
     * 이미지 S3 bucket 에 업로드
     * @param imageFile
     * @param dirName
     * @return
     */
    public Mono<String> uploadS3Image(Mono<FilePart> imageFile, String dirName) {
        return imageFile.flatMap(file -> {
            String originalFileName = file.filename();
            String fileName = dirName + "/" + UUID.randomUUID() + originalFileName.substring(originalFileName.lastIndexOf("."));

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .key(fileName)
                    .bucket(bucket)
                    .build();

            return DataBufferUtils.join(file.content()) // 모든 DataBuffer 를 하나로 합침
                    .map(dataBuffer -> {    // 합쳐진 DataBuffer 를 byte 배열로 변환
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        return bytes;
                    })
                    .flatMap(bytes -> Mono.fromFuture(s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes(bytes))))  // AsyncRequestBody 형태로 변환하고 putObject 에 넘겨 이미지 업로드
                    .then(Mono.just(String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName)));
        }).switchIfEmpty(Mono.just(""));    // 이미지를 전달받지 않았을 때
    }

    /**
     * S3 bucket 에서 이미지 목록 삭제
     * @param imageNames
     * @return
     */
    public Flux<Void> deleteS3ImageList(List<String> imageNames, String dirName) {
        return Flux.fromIterable(imageNames)
                .flatMap(imageName -> deleteS3Image(imageName, dirName));
    }

    /**
     * S3 bucket 에서 이미지 삭제
     * @param imageName
     * @return
     */
    public Mono<Void> deleteS3Image(String imageName, String dirName) {
        String fileName = imageName.substring(imageName.indexOf(dirName));
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .key(fileName)
                .bucket(bucket)
                .build();

        return Mono.fromFuture(s3AsyncClient.deleteObject(deleteObjectRequest))
                .onErrorResume(e -> {
                    log.error(fileName + " 파일 삭제에 실패했습니다.", e);
                    return Mono.empty();
                })
                .then();
    }

}
