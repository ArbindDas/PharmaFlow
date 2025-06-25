package com.JSR.PharmaFlow.Services;

import io.jsonwebtoken.io.IOException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final S3Client s3Client;


    @Value ( "${aws.s3.bucket}" )
    private String bucketName;

    @Autowired
    public S3Service( S3Client s3Client ) {
        this.s3Client=s3Client;
    }


    public String uploadFile( MultipartFile file ) throws IOException, java.io.IOException {
        String originalFileName = file.getOriginalFilename ();
        assert originalFileName != null;
        String fileExtension = originalFileName.substring ( originalFileName.lastIndexOf ( "." ) );
        String key =UUID.randomUUID () + fileExtension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket ( bucketName )
                .key ( key )
                .contentType ( file.getContentType () )
                .build();

        s3Client.putObject ( putObjectRequest,
                RequestBody.fromBytes ( file.getBytes () ));


        return  s3Client.utilities ().getUrl ( builder -> builder.bucket ( bucketName ).key ( key ) ).toString ();
    }


    public List<S3FileInfo> listAllFiles() {
        ListObjectsV2Request listObjectsReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsReq);

        return listObjectsResponse.contents().stream()
                .map(s3Object -> {
                    String url = s3Client.utilities().getUrl(builder ->
                            builder.bucket(bucketName).key(s3Object.key())).toString();
                    return new S3FileInfo(
                            s3Object.key(),
                            url,
                            s3Object.lastModified(),
                            s3Object.size()
                    );
                })
                .collect(Collectors.toList());
    }


    // Inner class to hold file information
    @Getter
    @Setter
    public static class S3FileInfo {
        private final String key;
        private final String url;
        private final Instant lastModified;
        private final long size;

        public S3FileInfo( String key, String url, Instant lastModified, long size) {
            this.key = key;
            this.url = url;
            this.lastModified = lastModified;
            this.size = size;
        }
    }


}


