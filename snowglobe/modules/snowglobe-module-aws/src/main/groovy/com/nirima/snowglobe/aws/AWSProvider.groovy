package com.nirima.snowglobe.aws

import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.nirima.snowglobe.core.Module
import com.nirima.snowglobe.core.Provider
import com.nirima.snowglobe.core.SGItem

/**
 * Provider for talking to Amazon Web Services
 */
@SGItem("aws_provider")
class AWSProvider extends Provider {

    public String region;

    public String access_key;
    public String secret_key;
    
    public String shared_credentials_file;

    public String profile;
    
    AWSProvider(Module module, String id,
                Closure closure) {
        super(module, id, closure)
    }

    public AmazonEC2 getEC2Client() {
        return AmazonEC2ClientBuilder.defaultClient();
//        ()
//        .withRegion(region)
//        .build();
    }
}