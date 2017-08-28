/*******************************************************************************
 * Copyright (c) 2017 Checkmarx
 * 
 * This software is licensed for customer's internal use only.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.checkmarx.engine.aws;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.checkmarx.engine.domain.EngineSize;
import com.google.common.base.MoreObjects;

@Component
@ConfigurationProperties(prefix="cx-aws-engine")
public class AwsEngineConfig {

	private boolean assignPublicIP;
	private int cxEngineTimeoutSec=300;
	private String cxVersion;
	private int engineExpireIntervalSecs = Math.toIntExact(TimeUnit.MINUTES.toSeconds(60));
	private String iamProfile;
	private String imageId;
	private String keyName;
	private int launchTimeoutSec=60;
	private int monitorPollingIntervalSecs = 10;
	private String securityGroup;
	private String subnetId;
	private boolean terminateOnStop;
	private boolean usePublicUrlForCx = false;
	private boolean usePublicUrlForMonitor = false;
	
	
	/**
	 * Maps EngineSize to EC2 instanceType; 
	 * 	key=size (name), 
	 * 	value=ec2 instance type (e.g. m4.large)
	 */
	private Map<String, String> engineSizeMap;
	
	public boolean isAssignPublicIP() {
		return assignPublicIP;
	}

	public void setAssignPublicIP(boolean assignPublicIP) {
		this.assignPublicIP = assignPublicIP;
	}

	/**
	 * Timeout for CxEngine response after EC2 instance enters Running state
	 */
	public int getCxEngineTimeoutSec() {
		return cxEngineTimeoutSec;
	}

	public void setCxEngineTimeoutSec(int cxEngineTimeoutSec) {
		this.cxEngineTimeoutSec = cxEngineTimeoutSec;
	}

	public String getCxVersion() {
		return cxVersion;
	}

	public void setCxVersion(String cxVersion) {
		this.cxVersion = cxVersion;
	}

	public String getIamProfile() {
		return iamProfile;
	}

	public void setIamProfile(String iamProfile) {
		this.iamProfile = iamProfile;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public int getEngineExpireIntervalSecs() {
		return engineExpireIntervalSecs;
	}

	public void setEngineExpireIntervalSecs(int instanceExpireIntervalSecs) {
		this.engineExpireIntervalSecs = instanceExpireIntervalSecs;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	/**
	 * Polling interval for monitoring instance/engine launch/startup 
	 */
	public int getMonitorPollingIntervalSecs() {
		return monitorPollingIntervalSecs;
	}

	public void setMonitorPollingIntervalSecs(int monitorPollingIntervalSecs) {
		this.monitorPollingIntervalSecs = monitorPollingIntervalSecs;
	}

	public String getSecurityGroup() {
		return securityGroup;
	}

	public void setSecurityGroup(String securityGroup) {
		this.securityGroup = securityGroup;
	}

	/**
	 * Timeout to wait for Running state after EC2 instance launch
	 */
	public int getLaunchTimeoutSec() {
		return launchTimeoutSec;
	}

	public void setLaunchTimeoutSec(int launchTimeoutSec) {
		this.launchTimeoutSec = launchTimeoutSec;
	}

	public String getSubnetId() {
		return subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}

	public boolean isTerminateOnStop() {
		return terminateOnStop;
	}

	public void setTerminateOnStop(boolean terminateOnStop) {
		this.terminateOnStop = terminateOnStop;
	}

	public boolean isUsePublicUrlForCx() {
		return usePublicUrlForCx;
	}

	public void setUsePublicUrlForCx(boolean usePublicUrlForCx) {
		this.usePublicUrlForCx = usePublicUrlForCx;
	}

	public boolean isUsePublicUrlForMonitor() {
		return usePublicUrlForMonitor;
	}

	public void setUsePublicUrlForMonitor(boolean usePublicUrlForMonitor) {
		this.usePublicUrlForMonitor = usePublicUrlForMonitor;
	}

	/**
	 * Map of EngineSize to EC2 instanceType; 
	 * key=size (name), 
	 * value=ec2 instance type (e.g. m4.large)
	 * @see EngineSize
	 */
	public Map<String, String> getEngineSizeMap() {
		return engineSizeMap;
	}

	public void setEngineSizeMap(Map<String, String> engineSizeMap) {
		this.engineSizeMap = engineSizeMap;
	}

	public String printEngineSizeMap() {
		final StringBuilder sb = new StringBuilder();
		engineSizeMap.forEach((size,instanceType) ->
			sb.append(String.format("%s->%s, ", size,instanceType)) );
		return sb.toString().replaceAll(", $", ""); 
	}

	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("assignPublicIP", assignPublicIP)
				.add("cxEngineTimeoutSec", cxEngineTimeoutSec)
				.add("cxVersion", cxVersion)
				.add("engineExpireIntervalSecs", engineExpireIntervalSecs)
				.add("iamProfile", iamProfile)
				.add("imageId", imageId)
				.add("keyName", keyName)
				.add("monitorPollingIntervalSecs", monitorPollingIntervalSecs)
				.add("securityGroup", securityGroup)
				.add("launchTimeoutSec", launchTimeoutSec)
				.add("subnetId", subnetId)
				.add("terminateOnStop", terminateOnStop)
				.add("usePublicUrlForCx", usePublicUrlForCx)
				.add("usePublicUrlForMonitor", usePublicUrlForMonitor)
				.add("engineSizeMap", "[" + printEngineSizeMap() +"]")
				.toString();
	}

}