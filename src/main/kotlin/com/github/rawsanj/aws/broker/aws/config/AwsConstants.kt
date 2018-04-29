package com.github.rawsanj.aws.broker.aws.config

object AwsConstants{

    val RDS_SERVICE_ID = "rds-service"
    val S3_SERVICE_ID = "s3-service"

    val RDS_DB_T2_MICRO_PLAN = "db.t2.micro"
    val RDS_DB_T2_MEDIUM_PLAN = "db.t2.medium"
    val RDS_DB_T2_LARGE_PLAN = "db.t2.large"

    val RDS_DB_INSTANCE_ID_STRING = "dbInstanceIdentifier"
    val MASTER_USERNAME_STRING = "masterUsername"
    val MASTER_PASSWORD_STRING = "masterUserPassword"
    val USERNAME_STRING = "username"
    val PASSWORD_STRING = "password"
    val HOSTNAME_STRING = "hostname"
    val PORT_STRING = "port"
    val DB_ALLOCATED_STORAGE_STRING = "allocatedStorage"
    val RDS_ENGINE_STRING = "engine"
    val AWS_REGION_STRING = "awsRegion"

    val S3_BUCKET_PLAN = "s3-bucket"
    val S3_BUCKET_NAME_STRING = "bucketName"

    val AWS_IAM_USER_STRING = "AWS_IAM_USER"
    val AWS_ARN_STRING = "AWS_ARN"
    val AWS_ACCESS_KEY_STRING = "AWS_ACCESS_KEY"
    val AWS_SECRET_KEY_STRING = "AWS_SECRET_KEY"

}
