/**
 * 
 */
package com.checkmarx.engine.domain;

import java.util.Map;
import java.util.Objects;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

/**
 * @author rjgeyer
 *
 */
public class DynamicEngine {
	
	private static final Logger log = LoggerFactory.getLogger(DynamicEngine.class);

	public enum State {
		ALL,
		SCANNING,
		EXPIRING,
		IDLE,
		UNPROVISIONED;
	}
	
	private final String name;
	private final String size;
	private State state = State.UNPROVISIONED;
	private DateTime currentStateTime = DateTime.now();
	private DateTime timeToExpire;
	private Host host;
	private Map<State, Duration> elapsedTimes = Maps.newConcurrentMap();
	private final int expireDurationSecs;
	private DateTime launchTime;
	private String scanRunId;
	private EnginePool enginePool;

	public void setEnginePool(EnginePool enginePool) {
		this.enginePool = enginePool;
	}

	public DynamicEngine(String name, String size, int expireDurationSecs) {
		this(name, size, expireDurationSecs, null);
	}
	
	public DynamicEngine(String name, String size, int expireDurationSecs, EnginePool enginePool) {
		this.name = name;
		this.size = size;
		this.expireDurationSecs = expireDurationSecs;
		this.enginePool = enginePool;
		initElapsedTimes();
	}
	
	public static DynamicEngine fromProvisionedInstance(
			String name, String size, int expireDurationSecs,
			DateTime launchTime, boolean isRunning) {
		final DynamicEngine engine = new DynamicEngine(name, size, expireDurationSecs);
		engine.launchTime = launchTime;
		if (isRunning) {
			engine.state = State.IDLE;
			engine.timeToExpire = engine.calcExpirationTime();
		}
		return engine;
	}
	
	private void initElapsedTimes() {
		final Duration zero = new Duration(0);
		for (State state : State.values()) {
			elapsedTimes.put(state, zero);
		}
	}

	public String getName() {
		return name;
	}

	public String getSize() {
		return size;
	}

	public State getState() {
		return state;
	}

	public Host getHost() {
		return host;
	}
	
	public String getUrl() {
		return host == null ? null : host.getUrl();
	}
	
	public DateTime getCurrentStateTime() {
		return currentStateTime;
	}
	
	public DateTime getLaunchTime() {
		return launchTime;
	}
	
	public DateTime getTimeToExpire() {
		return timeToExpire;
	}
	
	public void setState(State toState) {
		final State curState = this.state; 
		log.debug("setState(): currentState={}; newState={}; {}", curState, toState, this);
		
		//sanity check
		if (curState.equals(toState)) {
			log.warn("Setting DynamicEngine state to current state; state={}", toState);
			return;
		}
		
		// before changing state, update current state elapsed time
		final Duration currentDuration = elapsedTimes.get(this.state); 
		elapsedTimes.put(this.state, currentDuration.plus(getElapsedTime()));

		// if current state is UNPROVISIONED, set launch time
		if (curState.equals(State.UNPROVISIONED)) {
			launchTime = DateTime.now();
			if (host != null && host.getLaunchTime() != null) {
				launchTime = host.getLaunchTime();
			}
			timeToExpire = launchTime.plusSeconds(this.expireDurationSecs);
		}

		// update state
		this.state = toState;
		currentStateTime = DateTime.now();
		
		// if new state is UNPROVISIONED, clear applicable items
		switch (toState) {
			case UNPROVISIONED :
				host = null;
				launchTime = null;
				timeToExpire = null;
				break;
			case IDLE : 
				timeToExpire = calcExpirationTime();
				break;
			case SCANNING :
				timeToExpire = null;
				break;
			default:
				break;
		}
		if (enginePool != null) enginePool.changeState(this, curState, toState);
	}
	
	DateTime calcExpirationTime() {
		final Duration runTime = getRunTime();
		Long factor = Math.floorMod(runTime.getStandardSeconds(), expireDurationSecs) + 1;
		return launchTime.plusSeconds(factor.intValue() * expireDurationSecs);
	}

	/**
	 * Gets the elapsed time (duration) in the current state
	 * @return Duration since last state transition
	 */
	public Duration getElapsedTime() {
		return new Duration(currentStateTime, DateTime.now());  
	}
	
	/**
	 * Gets the elapsed time (duration) since the engine was launched.
	 * @return Duration since launched
	 */
	public Duration getRunTime() {
		if (launchTime == null) return Duration.ZERO;
		return new Duration(launchTime, DateTime.now());  
	}

	public void setHost(Host server) {
		this.host = server;
	}
	
	public String printElapsedTimes() {
		final StringBuilder sb = new StringBuilder();
		elapsedTimes.forEach((state,duration) -> {
			if (state.equals(State.ALL)) return;
			if (state.equals(this.state)) duration = duration.plus(getElapsedTime()); 
			sb.append(String.format("%s:%ss, ", state, duration.getStandardSeconds()));
		});
		return sb.toString().replaceAll(", $", "");
	}

	public String getScanRunId() {
		return scanRunId;
	}

	public void setScanRunId(String scanRunId) {
		this.scanRunId = scanRunId;
	}

	// name and size are only immutable properties
	@Override
	public int hashCode() {
		return Objects.hash(name, size);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final DynamicEngine other = (DynamicEngine) obj;
		return Objects.equals(this.name, other.name)
				&& Objects.equals(this.size, other.size);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("size", size)
				.add("state", state)
				.add("elapsedTime", getElapsedTime().getStandardSeconds())
				.add("launchTime", launchTime)
				.add("runTime", getRunTime().getStandardSeconds())
				.add("currentStateTime", currentStateTime)
				.add("expireDurationSecs", expireDurationSecs)
				.add("timeToExpire", timeToExpire)
				.add("scanRunId", scanRunId)
				.add("host", host)
				.add("elapsedTimes", "[" + printElapsedTimes() + "]")
				//.omitNullValues()
				.toString();
	}
	
}