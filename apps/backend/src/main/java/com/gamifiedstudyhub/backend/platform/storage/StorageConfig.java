package com.gamifiedstudyhub.backend.platform.storage;

import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Wires the active {@link StorageService}. The local provider is the default so the
 * app boots with zero cloud config; setting {@code app.storage.provider=s3} swaps in
 * the S3 client/presigner + {@link S3StorageService}. Both providers can target
 * S3/R2/MinIO via {@code app.storage.s3.endpoint}.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "local", matchIfMissing = true)
    public StorageService localStorageService(StorageProperties properties) {
        return new LocalStorageService(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "s3")
    public S3Client s3Client(StorageProperties properties) {
        StorageProperties.S3 s3 = properties.getS3();
        S3Configuration serviceConfig = S3Configuration.builder()
                .pathStyleAccessEnabled(s3.isPathStyleAccess())
                .build();

        var builder = S3Client.builder()
                .region(Region.of(s3.getRegion()))
                .serviceConfiguration(serviceConfig);
        if (s3.hasStaticCredentials()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())));
        }
        if (s3.hasCustomEndpoint()) {
            builder.endpointOverride(URI.create(s3.getEndpoint()));
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "s3")
    public S3Presigner s3Presigner(StorageProperties properties) {
        StorageProperties.S3 s3 = properties.getS3();
        var builder = S3Presigner.builder().region(Region.of(s3.getRegion()));
        if (s3.hasStaticCredentials()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())));
        }
        if (s3.hasCustomEndpoint()) {
            builder.endpointOverride(URI.create(s3.getEndpoint()));
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "s3")
    public StorageService s3StorageService(S3Client s3Client, S3Presigner s3Presigner, StorageProperties properties) {
        return new S3StorageService(s3Client, s3Presigner, properties);
    }
}
