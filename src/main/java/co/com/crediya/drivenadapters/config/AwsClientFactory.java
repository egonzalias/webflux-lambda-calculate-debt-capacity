package co.com.crediya.drivenadapters.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

public class AwsClientFactory {

    public static SqsClient createSqsClient() {
        return SqsClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }

    public static SnsAsyncClient createSnsAsyncClient() {
        return SnsAsyncClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }
}
