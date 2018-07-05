import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ec2.model.Reservation
import com.nirima.snowglobe.aws.AWSInstance
import com.nirima.snowglobe.aws.AWSInstanceState
import com.nirima.snowglobe.aws.AWSProvider
import com.nirima.snowglobe.core.Module

public class  TestEC2Launch {

    AWSProvider provider = new AWSProvider(null, "", {});
    Module module;

    public static void main(String[] parms) {

        TestEC2Launch t = new TestEC2Launch();

        t.create();

    }
    TestEC2Launch() {
        module = new Module(null,"",{});
    }

    public void create() {

        AWSInstance instance = new AWSInstance(module,"",{});

        AWSInstanceState is = new AWSInstanceState(instance, {});
        is.provider = provider;
        is.ami = "ami-5daa463a";
        is.instance_type = "t2.micro";
        
        instance.assess().create(is);

    }

    public void list() {



        provider.region = "eu-west-2";

        AmazonEC2 ec2 = provider.getEC2Client();

        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while(!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for(Reservation reservation : response.getReservations()) {
                for(Instance instance : reservation.getInstances()) {
                    System.out.printf(
                            "Found instance with id %s, " +
                            "AMI %s, " +
                            "type %s, " +
                            "state %s " +
                            "and monitoring state %s",
                            instance.getInstanceId(),
                            instance.getImageId(),
                            instance.getInstanceType(),
                            instance.getState().getName(),
                            instance.getMonitoring().getState());
                }
            }

            request.setNextToken(response.getNextToken());

            if(response.getNextToken() == null) {
                done = true;
            }
        }



    }

}
