package com.gamifiedstudyhub.backend.platform.storage;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for {@link StorageService}. Bound from {@code app.storage.*}.
 * Defaults keep the dev experience zero-config (local disk).
 */
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /** {@code local} (default) or {@code s3}. */
    private String provider = "local";

    private final Local local = new Local();
    private final S3 s3 = new S3();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Local getLocal() {
        return local;
    }

    public S3 getS3() {
        return s3;
    }

    public static class Local {
        /** Root directory for stored blobs. */
        private String basePath = "./data/storage";

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }

    public static class S3 {
        private String bucket;
        /** AWS region, or {@code auto} for R2. */
        private String region = "auto";
        /** Custom endpoint for R2/MinIO; leave blank for real AWS. */
        private String endpoint;
        private String accessKey;
        private String secretKey;
        /** Path-style access is required by MinIO and R2. */
        private boolean pathStyleAccess = true;
        /** TTL for presigned download URLs. */
        private Duration presignTtl = Duration.ofMinutes(15);

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public boolean isPathStyleAccess() {
            return pathStyleAccess;
        }

        public void setPathStyleAccess(boolean pathStyleAccess) {
            this.pathStyleAccess = pathStyleAccess;
        }

        public Duration getPresignTtl() {
            return presignTtl;
        }

        public void setPresignTtl(Duration presignTtl) {
            this.presignTtl = presignTtl;
        }

        public boolean hasStaticCredentials() {
            return accessKey != null && !accessKey.isBlank()
                    && secretKey != null && !secretKey.isBlank();
        }

        public boolean hasCustomEndpoint() {
            return endpoint != null && !endpoint.isBlank();
        }
    }
}
