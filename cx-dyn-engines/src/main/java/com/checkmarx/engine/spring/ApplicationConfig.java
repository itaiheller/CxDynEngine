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
package com.checkmarx.engine.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import com.checkmarx.engine.Config;
import com.checkmarx.engine.aws.AwsEngineConfig;
import com.checkmarx.engine.domain.DefaultEnginePoolBuilder;
import com.checkmarx.engine.domain.EnginePool;
import com.checkmarx.engine.domain.ScanQueue;
import com.checkmarx.engine.domain.EngineSize;
import com.checkmarx.engine.domain.EnginePool.EnginePoolEntry;
import com.checkmarx.engine.manager.EngineManager;
import com.checkmarx.engine.manager.EngineProvisioner;
import com.checkmarx.engine.manager.ScanQueueMonitor;
import com.checkmarx.engine.rest.CxRestClient;
import com.fasterxml.jackson.datatype.joda.JodaModule;

@Configuration
@EnableRetry
public class ApplicationConfig {
	
	@Bean
	public JodaModule jacksonJodaModule() {
		return new JodaModule();
	}
	
	@Bean
	public ScanQueue scansQueued(Config config) {
		return new ScanQueue(config.getQueueCapacity());
	}
	
	@Bean
	public ScanQueue scansFinished(Config config) {
		return new ScanQueue(config.getQueueCapacity());
	}
	
	@Bean
	public EnginePool enginePool(Config config, AwsEngineConfig awsConfig) {
		final DefaultEnginePoolBuilder builder = new DefaultEnginePoolBuilder(
				config.getEnginePoolPrefix(), 
				awsConfig.getEngineExpireIntervalSecs());
		awsConfig.getPool().forEach((entry) -> {
			builder.addEntry(entry);
		});
		return builder.build();
	}
	
	@Bean
	public EngineManager engineMonitor(
			Config config,
			EnginePool enginePool,
			CxRestClient cxClient,
			EngineProvisioner engineProvisioner,
			ScanQueue scansQueued, ScanQueue scansFinished) {
		
		return new EngineManager(config, enginePool, cxClient, engineProvisioner, 
						scansQueued.getQueue(), scansFinished.getQueue());
	}
	
	@Bean
	public ScanQueueMonitor queueMonitor(CxRestClient cxClient,
			ScanQueue scansQueued, ScanQueue scansFinished) {
		return new ScanQueueMonitor(scansQueued.getQueue(), scansFinished.getQueue(), cxClient);
	}
	
}
