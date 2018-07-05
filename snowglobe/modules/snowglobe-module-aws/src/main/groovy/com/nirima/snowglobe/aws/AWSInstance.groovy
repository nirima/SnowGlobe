package com.nirima.snowglobe.aws

import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ec2.model.InstanceType
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult
import com.nirima.snowglobe.core.Module
import com.nirima.snowglobe.core.Resource
import com.nirima.snowglobe.core.ResourceState
import com.nirima.snowglobe.core.SGItem
import com.nirima.snowglobe.plan.PlanAction
import com.nirima.snowglobe.plan.PlanActionBase
import groovy.util.logging.Slf4j

@SGItem("aws_instance")
public class AWSInstance extends Resource<AWSInstanceState> {


    AWSInstance(Module module, String id,
                Closure closure) {
        super(module, id, closure)
    }

    public PlanAction assess() {
        return new AWSInstanceAction(this);
    }
}
@Slf4j
public class AWSInstanceAction extends PlanActionBase<AWSInstance, AWSInstanceState> {

    AWSInstanceAction(Resource resource) {
        super(resource)
    }

    @Override
    AWSInstanceState read(AWSInstanceState desiredState) {
        log.info("Read {}", desiredState);

    }

    @Override
    AWSInstanceState create(AWSInstanceState desiredState) {

        log.info("Create {}", desiredState);

        AWSProvider provider = desiredState.getProvider();
        AmazonEC2 ec2 = provider.getEC2Client();

        RunInstancesRequest run_request = new RunInstancesRequest()
                .withImageId(desiredState.ami)
                .withInstanceType(InstanceType.fromValue(desiredState.instance_type))
                .withMaxCount(1)
                .withMinCount(1);


        RunInstancesResult run_response = ec2.runInstances(run_request);

        Instance awsInstance = run_response.getReservation().getInstances().get(0);

        desiredState.instance_id = awsInstance.getInstanceId();
        desiredState.public_ip   = awsInstance.getPublicIpAddress();
        desiredState.private_ip  = awsInstance.getPrivateIpAddress();


        String reservation_id = run_response.getReservation().getReservationId();

        return desiredState;

    }

    @Override
    AWSInstanceState update(AWSInstanceState old, AWSInstanceState newState) {

        if (old.compareTo(newState) != 0) {
            log.info "Docker Container ${newState.name} requires re-creation"
            // Delete and recreate
            delete(old);
            return create(newState);
        }

        // In the case of an update that does nothing, pass through any important
        // values.
        if (newState.id == null) {
            newState.id = old.id
        };

        // Old state will have stuff like IDs in it, so return that if it's deemed
        // to be the same.
        return old;

    }

    @Override
    AWSInstanceState delete(AWSInstanceState dockerContainer) {

        return null;
    }

}

public class AWSInstanceState  extends ResourceState implements Comparable {

    /**
     * ID of the instance
     */
    public String instance_id;

    /**
     * The AMI to use for this instance.
     */
    public String ami;

    /**
     * Instance Type (e.g t1.micro)
     */
    public String instance_type;

    /**
     * State of the instance ( started, stopped )
     */
    public String instance_state;

    /**
     * Private IP address.
     */
    public String private_ip;

    /**
     * Private IP address.
     */
    public String public_ip;

    /**
     *
     * @param parent
     * @param closure
     */

    AWSInstanceState(Resource parent, Closure closure) {
        super(parent, closure)
    }

    Closure getDefaults() {
        Closure defaults = {
            if( provider == null )
                provider = aws_provider(null);

            if( instance_state == null )
                instance_state = "started";
        }
        return defaults;
    }
}