package amazon_cloud;

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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.Volume;

/*These are the APIs*/
public class AWSInstanseManager {
	/*
	 * Important: Be sure to fill in your AWS access credentials in the
	 * AwsCredentials.properties file before you try to run this sample.
	 * http://aws.amazon.com/security-credentials
	 */
	static KeyPair keyPair;
	static double count = Math.random();
	static Scanner input = new Scanner(System.in);
	static AmazonEC2 ec2;

	public static void main(String[] args) throws Exception {

		AWSCredentials credentials = new PropertiesCredentials(
				AWSInstanseManager.class
						.getResourceAsStream("AwsCredentials.properties"));
		/*********************************************
		 * 
		 * #1 Create Amazon Client object
		 * 
		 *********************************************/
		System.out.println("#1 Create Amazon Client object");
		ec2 = new AmazonEC2Client(credentials);

		try {

			/*********************************************
			 * 
			 * #2 Describe Availability Zones.
			 * 
			 *********************************************/
			System.out.println("#2 Describe Availability Zones.");
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2
					.describeAvailabilityZones();

			System.out.println("You have access to " + availabilityZonesResult
					+ +availabilityZonesResult.getAvailabilityZones().size()
					+ " Availability Zones.");
			/*********************************************
			 * 
			 * #3 Describe Available Images
			 * 
			 *********************************************/

			System.out.println("#3 Describe Available Images");
			DescribeImagesResult dir = ec2.describeImages();
			List<Image> images = dir.getImages();
			System.out.println("You have " + images.size() + " Amazon images");

			/*********************************************
			 * 
			 * #4 Describe Key Pair
			 * 
			 *********************************************/
			System.out.println("#9 Describe Key Pair");
			DescribeKeyPairsResult dkr = ec2.describeKeyPairs();
			System.out.println(dkr.toString());

			/*********************************************
			 * 
			 * #5 Describe Current Instances
			 * 
			 *********************************************/
			System.out.println("#4 Describe Current Instances");
			DescribeInstancesResult describeInstancesRequest = ec2
					.describeInstances();
			List<Reservation> reservations = describeInstancesRequest
					.getReservations();
			Set<Instance> instances = new HashSet<Instance>();
			// add all instances to a Set.
			for (Reservation reservation : reservations) {
				instances.addAll(reservation.getInstances());
			}

			System.out.println("You have " + instances.size()
					+ " Amazon EC2 instance(s).");
			for (Instance ins : instances) {

				// instance id
				String instanceId = ins.getInstanceId();

				// instance state
				InstanceState is = ins.getState();
				System.out.println(instanceId + " " + is.getName());
			}

			/*********************************************
			 * 
			 * #6.1 Create a New Group
			 * 
			 *********************************************/
			String Temp_Group = "group" + count; // name of the group
			CreateSecurityGroupRequest r1 = new CreateSecurityGroupRequest(
					Temp_Group, "temporal group");
			ec2.createSecurityGroup(r1);
			AuthorizeSecurityGroupIngressRequest r2 = new AuthorizeSecurityGroupIngressRequest();
			r2.setGroupName(Temp_Group);

			/************* the property of http *****************/
			IpPermission permission = new IpPermission();
			permission.setIpProtocol("tcp");
			permission.setFromPort(80);
			permission.setToPort(80);
			List<String> ipRanges = new ArrayList<String>();
			ipRanges.add("0.0.0.0/0");
			permission.setIpRanges(ipRanges);

			/************* the property of SSH **********************/
			IpPermission permission1 = new IpPermission();
			permission1.setIpProtocol("tcp");
			permission1.setFromPort(22);
			permission1.setToPort(22);
			List<String> ipRanges1 = new ArrayList<String>();
			ipRanges1.add("0.0.0.0/0");
			permission1.setIpRanges(ipRanges1);

			/************* the property of https **********************/
			IpPermission permission2 = new IpPermission();
			permission2.setIpProtocol("tcp");
			permission2.setFromPort(443);
			permission2.setToPort(443);
			List<String> ipRanges2 = new ArrayList<String>();
			ipRanges2.add("0.0.0.0/0");
			permission2.setIpRanges(ipRanges2);

			/************* the property of tcp **********************/
			IpPermission permission3 = new IpPermission();
			permission3.setIpProtocol("tcp");
			permission3.setFromPort(0);
			permission3.setToPort(65535);
			List<String> ipRanges3 = new ArrayList<String>();
			ipRanges3.add("0.0.0.0/0");
			permission3.setIpRanges(ipRanges3);

			/********************** add rules to the group *********************/
			List<IpPermission> permissions = new ArrayList<IpPermission>();
			permissions.add(permission);
			permissions.add(permission1);
			permissions.add(permission2);
			permissions.add(permission3);
			r2.setIpPermissions(permissions);

			ec2.authorizeSecurityGroupIngress(r2);
			List<String> groupName = new ArrayList<String>();
			groupName.add(Temp_Group);// wait to out our instance into this
			// group

			/*********************************************
			 * 
			 * #6.2 Create a New Key Pair
			 * 
			 *********************************************/

			count++;
			CreateKeyPairRequest newKeyRequest = new CreateKeyPairRequest();
			newKeyRequest.setKeyName("New_Key" + count);
			CreateKeyPairResult keyresult = ec2.createKeyPair(newKeyRequest);

			/************************ print the properties of this key *****************/
			keyPair = keyresult.getKeyPair();
			System.out.println("The key we created is = "
					+ keyPair.getKeyName() + "\nIts fingerprint is="
					+ keyPair.getKeyFingerprint() + "\nIts material is= \n"
					+ keyPair.getKeyMaterial());

			/****************** store the key in a .pem file ****************/
			String fileName = "C:\\Users\\Anirban\\workspace\\AWSManager\\src\\assignment1\\"
					+ "New_Key" + count + ".pem";
			System.out.println(fileName);
			File file = new File(fileName);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedReader bufferedReader = new BufferedReader(
					new StringReader(keyPair.getKeyMaterial()));
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
					file));
			char buf[] = new char[1024];
			int len;
			while ((len = bufferedReader.read(buf)) != -1) {
				bufferedWriter.write(buf, 0, len);
			}
			bufferedWriter.flush();
			bufferedReader.close();
			bufferedWriter.close();

			/*********************************************
			 * 
			 * #6.3 Create an Instance
			 * 
			 *********************************************/
			System.out.println("#5 Create an Instance");
			String imageId = "ami-ab844dc2";// change to SUSE 64 bit Amazon AMI
			int minInstanceCount = 1; // create 1 instance
			int maxInstanceCount = 1;
			RunInstancesRequest rir = new RunInstancesRequest(imageId,
					minInstanceCount, maxInstanceCount);
			rir.setInstanceType("t1.micro");
			rir.setKeyName("New_Key" + count);// give the instance the key we
			// just created
			rir.setSecurityGroups(groupName);// set the instance in the group we
			// just created

			RunInstancesResult result = ec2.runInstances(rir);

			/***********
			 * to make sure the instance's state is
			 * "running instead of "pending",
			 **********/
			/*********** wait for a while **********/
			System.out.println("waiting");
			Thread.currentThread();
			Thread.sleep(50000);
			System.out.println("Instance Created");

			List<Instance> resultInstance = result.getReservation()
					.getInstances();

			String createdInstanceId = null;
			for (Instance ins : resultInstance) {

				createdInstanceId = ins.getInstanceId();
				System.out.println("New instance has been created: "
						+ ins.getInstanceId());// print the instance ID

				/*********************************************
				 * 
				 * #6.4 Create an Instance
				 * 
				 *********************************************/
				CreateVolumeRequest newVolumeRequest = new CreateVolumeRequest();
				newVolumeRequest.setSize(1); // 1.0GB
				System.out.println(availabilityZonesResult);
				newVolumeRequest.setAvailabilityZone("us-east-1a");// set its
				// available
				// zone, it
				// may
				// change.

				CreateVolumeResult volumeResult = ec2
						.createVolume(newVolumeRequest);

				Volume v1 = volumeResult.getVolume();
				String volumeID = v1.getVolumeId();
				AttachVolumeRequest avr = new AttachVolumeRequest();// begin to
				// attach
				// the
				// volume to
				// instance
				avr.withInstanceId(createdInstanceId);
				avr.withVolumeId(volumeID);
				avr.withDevice("/dev/sda2"); // mount it
				ec2.attachVolume(avr);
				System.out
						.println("EBS volume has been attached and the volume ID is: "
								+ volumeID);

				//

			}

			/*********************************************
			 * 
			 * #6.5 print public DNS and IP *
			 *********************************************/

			describeInstancesRequest = ec2.describeInstances();
			reservations = describeInstancesRequest.getReservations();
			int k = reservations.size();
			Reservation tempReservation = reservations.get(k - 1);
			Instance tempInstances = tempReservation.getInstances().get(0);
			System.out.println("The public DNS is: "
					+ tempInstances.getPublicDnsName() + "\n"
					+ tempInstances.getRamdiskId());
			System.out.println("The private IP is: "
					+ tempInstances.getPrivateIpAddress());
			System.out.println("The public IP is: "
					+ tempInstances.getPublicIpAddress());

			/*********************************************
			 * 
			 * #7 Create a 'tag' for the new instance.
			 * 
			 *********************************************/
			System.out.println("#6 Create a 'tag' for the new instance.");
			List<String> resources = new LinkedList<String>();
			List<Tag> tags = new LinkedList<Tag>();
			Tag nameTag = new Tag("Name", "MyFirstInstance" + count);

			resources.add(createdInstanceId);
			tags.add(nameTag);

			CreateTagsRequest ctr = new CreateTagsRequest(resources, tags);
			ec2.createTags(ctr);

			/*********************************************
			 * 
			 * #8 Stop an Instance
			 * 
			 *********************************************/
			List<String> instanceIds = new LinkedList<String>();
			instanceIds.add(createdInstanceId);
			System.out.println("Do you want to stop the instance? y/n ");
			String s = input.nextLine();
			System.out.println("Stop the instance.." + s);
			if (s.equals("y")) {
				// stop
				System.out.println("Stopping the instance.. ");
				StopInstancesRequest stopIR = new StopInstancesRequest(
						instanceIds);
				ec2.stopInstances(stopIR);
				System.out.println("Stoping");
				Thread.currentThread();
				Thread.sleep(50000);
				/*********************************************
				 * 
				 * #9 Start an Instance
				 * 
				 *********************************************/
				// start
				System.out.println("Do you want to start the instance? y/n ");
				String st = input.nextLine();
				System.out.println("start the instance " + st);
				StartInstancesRequest startIR = new StartInstancesRequest(
						instanceIds);
				ec2.startInstances(startIR);
				System.out.println("Starting");
				Thread.currentThread();
				Thread.sleep(50000);
			}
			/*********************************************
			 * 
			 * #10 Reboot an Instance
			 * 
			 *********************************************/
			System.out.println("Do you want to reboot the instance? y/n ");
			String ro = input.nextLine();
			System.out.println("reboot the instance " + ro);
			if (ro.equals("y")) {
				System.out.println("#9 Rebooting the Instance");
				RebootInstancesRequest rb = new RebootInstancesRequest(
						instanceIds);
				ec2.rebootInstances(rb);
				System.out.println("Rebooting");
				Thread.currentThread();
				Thread.sleep(50000);
				System.out.println("Rebooted");
			}
			/*********************************************
			 * 
			 * #11 Terminate an Instance
			 * 
			 *********************************************/
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
				/*********************************************
				 * 
				 * #12 shutdown client object
				 * 
				 *********************************************/
				ec2.shutdown();
				System.out.println("Instance Terminated");
			}

		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}

	}
}