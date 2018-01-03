/**
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
 */
package com.checkmarx.engine.domain;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.checkmarx.engine.domain.EnginePool.EnginePoolEntry;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

/**
 * Engine pool configuration
 * 
 * @author rjgey
 */
@Configuration
@ConfigurationProperties(prefix="cx-engine")
public class EnginePoolConfig {

	private final List<EnginePoolEntry> pool = Lists.newArrayList();
	
	private int engineExpireIntervalSecs = Math.toIntExact(TimeUnit.MINUTES.toSeconds(60));
	private String enginePrefix = "cx-engine";

	public int getEngineExpireIntervalSecs() {
		return engineExpireIntervalSecs;
	}

	public void setEngineExpireIntervalSecs(int engineExpireIntervalSecs) {
		this.engineExpireIntervalSecs = engineExpireIntervalSecs;
	}

	/**
	 * @return The prefix used for IAAS engine server names.
	 * 		Default value is {@code cx-engine}.
	 */
	public String getEnginePrefix() {
		return enginePrefix;
	}

	public void setEnginePrefix(String enginePrefix) {
		this.enginePrefix = enginePrefix;
	}

	public List<EnginePoolEntry> getPool() {
		return pool;
	}

	private String printEnginePool() {
		final StringBuilder sb = new StringBuilder();
		pool.forEach((entry) -> {
			sb.append(String.format("%s:%d:%d, ", 
					entry.getScanSize().getName(), entry.getMinimum(), entry.getCount()));
		});
		return sb.toString().replaceAll(", $", ""); 
	}

	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("engineExpireIntervalSecs", engineExpireIntervalSecs)
				.add("enginePrefix", getEnginePrefix())
				.add("enginePool", "[" + printEnginePool() + "]")
				.toString();
	}

}
