/**
 * Welcome to your new AWS Java SDK based project!
 *
 * This class is meant as a starting point for your console-based application that
 * makes one or more calls to the AWS services supported by the Java SDK, such as EC2,
 * SimpleDB, and S3.
 *
 * In order to use the services in this sample, you need:
 *
 *  - A valid Amazon Web Services account. You can register for AWS at:
 *       https://aws-portal.amazon.com/gp/aws/developer/registration/index.html
 *
 *  - Your account's Access Key ID and Secret Access Key:
 *       http://aws.amazon.com/security-credentials
 *
 *  - A subscription to Amazon EC2. You can sign up for EC2 at:
 *       http://aws.amazon.com/ec2/
 *
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AttachInternetGatewayRequest;
import com.amazonaws.services.ec2.model.CreateSubnetRequest;
import com.amazonaws.services.ec2.model.CreateSubnetResult;
import com.amazonaws.services.ec2.model.CreateVpcRequest;
import com.amazonaws.services.ec2.model.CreateVpcResult;
import com.amazonaws.services.ec2.model.DeleteVpcRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.Vpc;

public class VPCManager {

	/*
	 * Important: Be sure to fill in your AWS access credentials in the
	 * AwsCredentials.properties file before you try to run this sample.
	 * http://aws.amazon.com/security-credentials
	 */

	static AmazonEC2 ec2;
	static Scanner input = new Scanner(System.in);

	public static void main(String[] args) {
		try {
			AWSCredentials credentials = new PropertiesCredentials(
					VPCManager.class
							.getResourceAsStream("AwsCredentials.properties"));
			ec2 = new AmazonEC2Client(credentials);

			// constants
			final String vpcCIDR = "10.0.0.0/16";
			final String subNetCIDR = "10.0.0.0/24";
			final String ami = "ami-1624987f";
			// This is account specific
			final String keyNm = "New_Key1";
			final String secGrpNm = "anirban101";

			/*********************************************
			 * 
			 * #1 Create Virtual Private Cloud
			 * 
			 *********************************************/
			// CreateVPC
			CreateVpcRequest createVpcRequest = new CreateVpcRequest(vpcCIDR);
			CreateVpcResult createVpcResult = ec2.createVpc(createVpcRequest);
			Vpc vpc = createVpcResult.getVpc();

			// create public subnet for the vpc
			CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest(
					vpc.getVpcId(), subNetCIDR);
			CreateSubnetResult createSubnetResult = ec2
					.createSubnet(createSubnetRequest);
			Subnet subnet = createSubnetResult.getSubnet();

			/*********************************************
			 * 
			 * #2 Create Virtual Private Cloud Instance
			 * 
			 *********************************************/
			// create vpc instance
			int minInstanceCount = 1; // create 1 instance
			int maxInstanceCount = 1;
			RunInstancesRequest rir = new RunInstancesRequest(ami,
					minInstanceCount, maxInstanceCount);
			rir.setSubnetId(subnet.getSubnetId());
			ArrayList<String> secGrp = new ArrayList<String>();
			secGrp.add(secGrpNm);
			rir.setKeyName(keyNm);

			RunInstancesResult result = ec2.runInstances(rir);

			// get instanceId from the result
			List<Instance> resultInstance = result.getReservation()
					.getInstances();
			String createdInstanceId = null;
			for (Instance ins : resultInstance) {
				createdInstanceId = ins.getInstanceId();
				System.out.println("New instance has been created: "
						+ ins.getInstanceId());
			}

			String state = "";
			DescribeInstancesRequest describeReq = new DescribeInstancesRequest();
			List<String> resources = new LinkedList<String>();
			resources.add(createdInstanceId);
			describeReq.setInstanceIds(resources);
			Instance ins = null;
			while (!state.equals("running")) {
				DescribeInstancesResult describeInstancesResult = ec2
						.describeInstances(describeReq);
				List<Reservation> reservations = describeInstancesResult
						.getReservations();
				Reservation res = (Reservation) reservations.get(0);
				ins = res.getInstances().get(0);
				InstanceState is = ins.getState();
				state = is.getName();
				Thread.sleep(50000);
			}

			/*********************************************
			 * 
			 * #3 Create Internet Gateway
			 * 
			 *********************************************/
			// create internet gateway
			InternetGateway gateway = ec2.createInternetGateway()
					.getInternetGateway();
			// Attach Internet Gateway
			AttachInternetGatewayRequest attachGatewayReq = new AttachInternetGatewayRequest();
			attachGatewayReq.setVpcId(vpc.getVpcId());
			attachGatewayReq.setInternetGatewayId(gateway
					.getInternetGatewayId());
			ec2.attachInternetGateway(attachGatewayReq);

			/*********************************************
			 * 
			 * #4 Allocate Elastic IP
			 * 
			 *********************************************/
			// allocate elastic ip
			AllocateAddressRequest elasticRequest = new AllocateAddressRequest();
			elasticRequest.setDomain("vpc");
			AllocateAddressResult elasticResult = ec2
					.allocateAddress(elasticRequest);
			String elasticIp = elasticResult.getPublicIp();
			System.out.println("New elastic IP: " + elasticIp);

			/*********************************************
			 * 
			 * #5 Associate Elastic IP
			 * 
			 *********************************************/
			// associate elastic ip
			AssociateAddressRequest aar = new AssociateAddressRequest();
			aar.setInstanceId(createdInstanceId);
			aar.setAllocationId(elasticResult.getAllocationId());
			ec2.associateAddress(aar);

			/*********************************************
			 * 
			 * #6 Delete VPC
			 * 
			 *********************************************/
			List<String> instanceIds = new LinkedList<String>();
			instanceIds.add(createdInstanceId);
			System.out.println("Do you want to terminate the instance? y/n ");
			String t = input.nextLine();
			System.out.println("Terminating the instance " + t);
			if (t.equals("y")) {
				System.out.println("#8 Terminate the Instance");
				TerminateInstancesRequest tir = new TerminateInstancesRequest(
						instanceIds);
				ec2.terminateInstances(tir);
				System.out.println("Terminating");
				Thread.currentThread();
				Thread.sleep(50000);

				System.out.println("Do you want to delete vpc? y/n ");
				String d = input.nextLine();
				System.out.println("Deleting vpc " + d);
				DeleteVpcRequest deleteVpcRequest = new DeleteVpcRequest(
						vpcCIDR);
				deleteVpcRequest.withVpcId(vpc.getVpcId());
				Thread.currentThread();
				Thread.sleep(50000);
				System.out.println("Deleted VPC" + vpc.getVpcId());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
}
