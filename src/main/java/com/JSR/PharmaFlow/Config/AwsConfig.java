package com.JSR.PharmaFlow.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {


    @Value ( "${aws.accessKeyId}" ) // Will resolve from either property source
    private String accessKey;

    @Value ( "${aws.secretKey}" )
    private String secretKey;

    @Value ( "${aws.region}" )
    private String region;


//     S3Client.builder() – Starts building the S3 client.
//
//    .region(Region.of(region)) – Sets the AWS region (e.g., us-east-1). region is a variable holding your region string.
//
//    .credentialsProvider(...) – Supplies the AWS credentials using a static provider.
//
//    .build() – Finalizes and returns the S3Client instance.


    @Bean
    public S3Client s3Client( ) {
        AwsBasicCredentials awsCreds=AwsBasicCredentials.create ( accessKey , secretKey );

        return S3Client.builder ( )
                .region ( Region.of ( region ) )
                .credentialsProvider ( StaticCredentialsProvider.create ( awsCreds ) )
                .build ( );
    }

    // ** This method creates an Amazon S3 client configured with static credentials and region, and registers it
// ** as a Spring Bean so it can be autowired and used anywhere in your Spring Boot app.
    @Bean
    public S3Presigner s3Presigner( ) {

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create ( accessKey , secretKey );
        return  S3Presigner.builder ()
                .region ( Region.of ( region ) )
                .credentialsProvider ( StaticCredentialsProvider.create ( awsCreds ) )
                .build ();
    }
}
