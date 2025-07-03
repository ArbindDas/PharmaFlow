package com.JSR.PharmaFlow.Controllers;


import com.JSR.PharmaFlow.Config.AwsConfig;
import com.JSR.PharmaFlow.Services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {



    private final S3Service s3Service;

    @Autowired
    public FileUploadController( S3Service s3Service ) {
        this.s3Service=s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            String fileUrl = s3Service.uploadFile(file);
            return ResponseEntity.ok( Map.of("url", fileUrl));

        } catch (IOException e) {
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        }
    }


    @GetMapping("/getListOfFiles")
    public ResponseEntity<List< S3Service.S3FileInfo >> listFiles(){
        List< S3Service.S3FileInfo > fileInfos = s3Service.listAllFiles ();
        return ResponseEntity.ok ( fileInfos );
    }


    @GetMapping("/presigned-url")
    public ResponseEntity<?> generatePresignedUrl(@RequestParam String fileName){
            try {
                String presignedUrl = s3Service.preSignedUrl ( fileName );
                return ResponseEntity.ok ( Map.of ( "presignedUrl", presignedUrl ) );

            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to generate pre-signed URL: " + e.getMessage());
            }
    }
}
