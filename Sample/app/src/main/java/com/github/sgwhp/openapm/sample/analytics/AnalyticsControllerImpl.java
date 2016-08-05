package com.github.sgwhp.openapm.sample.analytics;

import com.github.sgwhp.openapm.sample.AgentConfiguration;
import com.github.sgwhp.openapm.sample.AgentImpl;
import com.github.sgwhp.openapm.sample.harvest.DeviceInformation;
import com.github.sgwhp.openapm.sample.harvest.EnvironmentInformation;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.tracing.ActivityTrace;
import com.github.sgwhp.openapm.sample.tracing.TraceLifecycleAware;
import com.github.sgwhp.openapm.sample.tracing.TraceMachine;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by user on 2016/8/2.
 */
public class AnalyticsControllerImpl implements AnalyticsController{


    protected static final int MAX_ATTRIBUTES = 64;
    static final AgentLog log;
    private Set<AnalyticAttribute> systemAttributes;
    private Set<AnalyticAttribute> userAttributes;
    private EventManager eventManager;
    private boolean isEnabled;
    private AgentImpl agentImpl;
    private AgentConfiguration agentConfiguration;
    private InteractionCompleteListener listener;
    private static final AnalyticsControllerImpl instance;
    private static final AtomicBoolean initialized;
    private static final List<String> reservedNames;
    private static final String NEW_RELIC_PREFIX = "newRelic";
    private static final String NR_PREFIX = "nr.";

    public static void initialize(final AgentConfiguration agentConfiguration, final AgentImpl agentImpl) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.initialize invoked.");
        if (!AnalyticsControllerImpl.initialized.compareAndSet(false, true)) {
            AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl has already been initialized.  Bypassing..");
            return;
        }
        AnalyticsControllerImpl.instance.clear();
        AnalyticsControllerImpl.reservedNames.add("eventType");
        AnalyticsControllerImpl.reservedNames.add("type");
        AnalyticsControllerImpl.reservedNames.add("timestamp");
        AnalyticsControllerImpl.reservedNames.add("category");
        AnalyticsControllerImpl.reservedNames.add("accountId");
        AnalyticsControllerImpl.reservedNames.add("appId");
        AnalyticsControllerImpl.reservedNames.add("appName");
        AnalyticsControllerImpl.reservedNames.add("uuid");
        AnalyticsControllerImpl.reservedNames.add("sessionId");
        AnalyticsControllerImpl.reservedNames.add("osName");
        AnalyticsControllerImpl.reservedNames.add("osVersion");
        AnalyticsControllerImpl.reservedNames.add("osMajorVersion");
        AnalyticsControllerImpl.reservedNames.add("deviceManufacturer");
        AnalyticsControllerImpl.reservedNames.add("deviceModel");
        AnalyticsControllerImpl.reservedNames.add("memUsageMb");
        AnalyticsControllerImpl.reservedNames.add("carrier");
        AnalyticsControllerImpl.reservedNames.add("newRelicVersion");
        AnalyticsControllerImpl.reservedNames.add("interactionDuration");
        AnalyticsControllerImpl.reservedNames.add("install");
        AnalyticsControllerImpl.reservedNames.add("upgradeFrom");
        AnalyticsControllerImpl.reservedNames.add("platform");
        AnalyticsControllerImpl.reservedNames.add("platformVersion");
        AnalyticsControllerImpl.instance.reinitialize(agentConfiguration, agentImpl);
        TraceMachine.addTraceListener(AnalyticsControllerImpl.instance.listener);
        AnalyticsControllerImpl.log.info("Analytics Controller started.");
    }

    public static void shutdown() {
        TraceMachine.removeTraceListener(AnalyticsControllerImpl.instance.listener);
        AnalyticsControllerImpl.initialized.compareAndSet(true, false);
        AnalyticsControllerImpl.instance.getEventManager().shutdown();
    }

    private AnalyticsControllerImpl() {
        this.eventManager = new EventManagerImpl();
        this.systemAttributes = Collections.synchronizedSet(new HashSet<AnalyticAttribute>());
        this.userAttributes = Collections.synchronizedSet(new HashSet<AnalyticAttribute>());
        this.listener = new InteractionCompleteListener();
    }

    void reinitialize(final AgentConfiguration agentConfiguration, final AgentImpl agentImpl) {
        this.agentImpl = agentImpl;
        this.agentConfiguration = agentConfiguration;
        this.eventManager.initialize();
        this.isEnabled = agentConfiguration.getEnableAnalyticsEvents();
        this.loadPersistentAttributes();
        final DeviceInformation deviceInformation = agentImpl.getDeviceInformation();
        String osVersion = deviceInformation.getOsVersion();
        osVersion = osVersion.replace(" ", "");
        final String[] osMajorVersionArr = osVersion.split("[.:-]");
        String osMajorVersion;
        if (osMajorVersionArr.length > 0) {
            osMajorVersion = osMajorVersionArr[0];
        }
        else {
            osMajorVersion = osVersion;
        }
        final EnvironmentInformation environmentInformation = agentImpl.getEnvironmentInformation();
        this.systemAttributes.add(new AnalyticAttribute("osName", deviceInformation.getOsName()));
        this.systemAttributes.add(new AnalyticAttribute("osVersion", osVersion));
        this.systemAttributes.add(new AnalyticAttribute("osMajorVersion", osMajorVersion));
        this.systemAttributes.add(new AnalyticAttribute("deviceManufacturer", deviceInformation.getManufacturer()));
        this.systemAttributes.add(new AnalyticAttribute("deviceModel", deviceInformation.getModel()));
        this.systemAttributes.add(new AnalyticAttribute("uuid", deviceInformation.getDeviceId()));
        this.systemAttributes.add(new AnalyticAttribute("carrier", agentImpl.getNetworkCarrier()));
        this.systemAttributes.add(new AnalyticAttribute("newRelicVersion", deviceInformation.getAgentVersion()));
        this.systemAttributes.add(new AnalyticAttribute("memUsageMb", environmentInformation.getMemoryUsage()));
        this.systemAttributes.add(new AnalyticAttribute("sessionId", agentConfiguration.getSessionID()));
        this.systemAttributes.add(new AnalyticAttribute("platform", agentConfiguration.getApplicationPlatform().toString()));
        this.systemAttributes.add(new AnalyticAttribute("platformVersion", agentConfiguration.getApplicationPlatformVersion()));
    }

    @Override
    public AnalyticAttribute getAttribute(final String name) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.getAttribute - retrieving " + name);
        AnalyticAttribute attribute = this.getUserAttribute(name);
        if (attribute == null) {
            attribute = this.getSystemAttribute(name);
        }
        return attribute;
    }

    @Override
    public Set<AnalyticAttribute> getSystemAttributes() {
        final Set<AnalyticAttribute> attrs = new HashSet<AnalyticAttribute>(this.systemAttributes.size());
        for (final AnalyticAttribute attr : this.systemAttributes) {
            attrs.add(new AnalyticAttribute(attr));
        }
        return Collections.unmodifiableSet((Set<? extends AnalyticAttribute>)attrs);
    }

    @Override
    public Set<AnalyticAttribute> getUserAttributes() {
        final Set<AnalyticAttribute> attrs = new HashSet<AnalyticAttribute>(this.userAttributes.size());
        for (final AnalyticAttribute attr : this.userAttributes) {
            attrs.add(new AnalyticAttribute(attr));
        }
        return Collections.unmodifiableSet((Set<? extends AnalyticAttribute>)attrs);
    }

    @Override
    public Set<AnalyticAttribute> getSessionAttributes() {
        final Set<AnalyticAttribute> attrs = new HashSet<AnalyticAttribute>(this.getSessionAttributeCount());
        attrs.addAll(this.getSystemAttributes());
        attrs.addAll(this.getUserAttributes());
        return Collections.unmodifiableSet((Set<? extends AnalyticAttribute>)attrs);
    }

    @Override
    public int getSystemAttributeCount() {
        return this.systemAttributes.size();
    }

    @Override
    public int getUserAttributeCount() {
        return this.userAttributes.size();
    }

    @Override
    public int getSessionAttributeCount() {
        return this.systemAttributes.size() + this.userAttributes.size();
    }

    @Override
    public boolean setAttribute(final String name, final String value) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.setAttribute - " + name + ": " + value);
        return this.setAttribute(name, value, true);
    }

    @Override
    public boolean setAttribute(final String name, final String value, final boolean persistent) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.setAttribute - " + name + ": " + value + (persistent ? " (persistent)" : " (transient)"));
        if (!this.isInitializedAndEnabled()) {
            return false;
        }
        if (!this.isAttributeNameValid(name) || !this.isStringValueValid(name, value)) {
            return false;
        }
        AnalyticAttribute attribute = this.getAttribute(name);
        if (attribute == null) {
            if (this.userAttributes.size() < 64) {
                attribute = new AnalyticAttribute(name, value, persistent);
                this.userAttributes.add(attribute);
                if (attribute.isPersistent()) {
                    final boolean stored = this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                    if (!stored) {
                        AnalyticsControllerImpl.log.error("Failed to store attribute " + attribute + " to attribute store.");
                        return stored;
                    }
                }
            }
            else {
                AnalyticsControllerImpl.log.warning("Attribute limit exceeded: at most 64 are allowed.");
                AnalyticsControllerImpl.log.warning("Currently defined attributes:");
                for (final AnalyticAttribute attr : this.userAttributes) {
                    AnalyticsControllerImpl.log.warning("\t" + attr.getName() + ": " + attr.valueAsString());
                }
            }
        }
        else {
            attribute.setStringValue(value);
            attribute.setPersistent(persistent);
            if (attribute.isPersistent()) {
                final boolean stored = this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                if (!stored) {
                    AnalyticsControllerImpl.log.error("Failed to store attribute " + attribute + " to attribute store.");
                    return stored;
                }
            }
            else {
                this.agentConfiguration.getAnalyticAttributeStore().delete(attribute);
            }
        }
        return true;
    }

    @Override
    public boolean setAttribute(final String name, final float value) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.setAttribute - " + name + ": " + value);
        return this.setAttribute(name, value, true);
    }

    @Override
    public boolean setAttribute(final String name, final float value, final boolean persistent) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.setAttribute - " + name + ": " + value + (persistent ? " (persistent)" : " (transient)"));
        if (!this.isInitializedAndEnabled()) {
            return false;
        }
        if (!this.isAttributeNameValid(name)) {
            return false;
        }
        AnalyticAttribute attribute = this.getAttribute(name);
        if (attribute == null) {
            if (this.userAttributes.size() < 64) {
                attribute = new AnalyticAttribute(name, value, persistent);
                this.userAttributes.add(attribute);
                if (attribute.isPersistent()) {
                    this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                    final boolean stored = this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                    if (!stored) {
                        AnalyticsControllerImpl.log.error("Failed to store attribute " + attribute + " to attribute store.");
                        return stored;
                    }
                }
            }
            else {
                AnalyticsControllerImpl.log.warning("Attribute limit exceeded: at most 64 are allowed.");
                AnalyticsControllerImpl.log.warning("Currently defined attributes:");
                for (final AnalyticAttribute attr : this.userAttributes) {
                    AnalyticsControllerImpl.log.warning("\t" + attr.getName() + ": " + attr.valueAsString());
                }
            }
        }
        else {
            attribute.setFloatValue(value);
            attribute.setPersistent(persistent);
            if (attribute.isPersistent()) {
                final boolean stored = this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                if (!stored) {
                    AnalyticsControllerImpl.log.error("Failed to store attribute " + attribute + " to attribute store.");
                    return stored;
                }
            }
            else {
                this.agentConfiguration.getAnalyticAttributeStore().delete(attribute);
            }
        }
        return true;
    }

    @Override
    public boolean setAttribute(final String name, final boolean value) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.setAttribute - " + name + ": " + value);
        return this.setAttribute(name, value, true);
    }

    @Override
    public boolean setAttribute(final String name, final boolean value, final boolean persistent) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.setAttribute - " + name + ": " + value + (persistent ? " (persistent)" : " (transient)"));
        if (!this.isInitializedAndEnabled()) {
            return false;
        }
        if (!this.isAttributeNameValid(name)) {
            return false;
        }
        AnalyticAttribute attribute = this.getAttribute(name);
        if (attribute == null) {
            if (this.userAttributes.size() < 64) {
                attribute = new AnalyticAttribute(name, value, persistent);
                this.userAttributes.add(attribute);
                if (attribute.isPersistent()) {
                    final boolean stored = this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                    if (!stored) {
                        AnalyticsControllerImpl.log.error("Failed to store attribute " + attribute + " to attribute store.");
                        return stored;
                    }
                }
            }
            else {
                AnalyticsControllerImpl.log.warning("Attribute limit exceeded: at most 64 are allowed.");
                AnalyticsControllerImpl.log.warning("Currently defined attributes:");
                for (final AnalyticAttribute attr : this.userAttributes) {
                    AnalyticsControllerImpl.log.warning("\t" + attr.getName() + ": " + attr.valueAsString());
                }
            }
        }
        else {
            attribute.setBooleanValue(value);
            attribute.setPersistent(persistent);
            if (attribute.isPersistent()) {
                final boolean stored = this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                if (!stored) {
                    AnalyticsControllerImpl.log.error("Failed to store attribute " + attribute + " to attribute store.");
                    return stored;
                }
            }
            else {
                this.agentConfiguration.getAnalyticAttributeStore().delete(attribute);
            }
        }
        return true;
    }

    public boolean addAttributeUnchecked(final AnalyticAttribute attribute, final boolean persistent) {
        final String name = attribute.getName();
        final String value = attribute.valueAsString();
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.setAttributeUnchecked - " + name + ": " + value + (persistent ? " (persistent)" : " (transient)"));
        if (!AnalyticsControllerImpl.initialized.get()) {
            AnalyticsControllerImpl.log.warning("Analytics controller is not initialized!");
            return false;
        }
        if (!this.isEnabled) {
            AnalyticsControllerImpl.log.warning("Analytics controller is not enabled!");
            return false;
        }
        if (!this.isNameValid(name)) {
            return false;
        }
        final AnalyticAttribute foundAttribute = this.getAttribute(attribute.getName());
        if (foundAttribute == null) {
            this.userAttributes.add(attribute);
            if (attribute.isPersistent()) {
                this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                final boolean stored = this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                if (!stored) {
                    AnalyticsControllerImpl.log.error("Failed to store attribute " + attribute + " to attribute store.");
                    return stored;
                }
            }
        }
        else {
            switch (attribute.getAttributeDataType()) {
                case STRING: {
                    foundAttribute.setStringValue(attribute.getStringValue());
                    break;
                }
                case FLOAT: {
                    foundAttribute.setFloatValue(attribute.getFloatValue());
                    break;
                }
                case BOOLEAN: {
                    foundAttribute.setBooleanValue(attribute.getBooleanValue());
                    break;
                }
            }
            foundAttribute.setPersistent(persistent);
            if (foundAttribute.isPersistent()) {
                final boolean stored = this.agentConfiguration.getAnalyticAttributeStore().store(foundAttribute);
                if (!stored) {
                    AnalyticsControllerImpl.log.error("Failed to store attribute " + foundAttribute + " to attribute store.");
                    return stored;
                }
            }
            else {
                this.agentConfiguration.getAnalyticAttributeStore().delete(foundAttribute);
            }
        }
        return true;
    }

    @Override
    public boolean incrementAttribute(final String name, final float value) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.incrementAttribute - " + name + ": " + value);
        return this.incrementAttribute(name, value, true);
    }

    @Override
    public boolean incrementAttribute(final String name, final float value, final boolean persistent) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.incrementAttribute - " + name + ": " + value + (persistent ? " (persistent)" : " (transient)"));
        if (!this.isInitializedAndEnabled()) {
            return false;
        }
        if (!this.isAttributeNameValid(name)) {
            return false;
        }
        AnalyticAttribute attribute = this.getAttribute(name);
        if (attribute != null && attribute.isFloatAttribute()) {
            attribute.setFloatValue(attribute.getFloatValue() + value);
            attribute.setPersistent(persistent);
            if (attribute.isPersistent()) {
                final boolean stored = this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                if (!stored) {
                    AnalyticsControllerImpl.log.error("Failed to store attribute " + attribute + " to attribute store.");
                    return stored;
                }
            }
        }
        else {
            if (attribute != null) {
                AnalyticsControllerImpl.log.warning("Cannot increment attribute " + name + ": the attribute is already defined as a non-float value.");
                return false;
            }
            if (this.userAttributes.size() < 64) {
                attribute = new AnalyticAttribute(name, value, persistent);
                this.userAttributes.add(attribute);
                if (attribute.isPersistent()) {
                    this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                    final boolean stored = this.agentConfiguration.getAnalyticAttributeStore().store(attribute);
                    if (!stored) {
                        AnalyticsControllerImpl.log.error("Failed to store attribute " + attribute + " to attribute store.");
                        return stored;
                    }
                }
                else {
                    this.agentConfiguration.getAnalyticAttributeStore().delete(attribute);
                }
            }
        }
        return true;
    }

    @Override
    public boolean removeAttribute(final String name) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.removeAttribute - " + name);
        if (!this.isInitializedAndEnabled()) {
            return false;
        }
        final AnalyticAttribute attribute = this.getAttribute(name);
        if (attribute != null) {
            this.userAttributes.remove(attribute);
            if (attribute.isPersistent()) {
                this.agentConfiguration.getAnalyticAttributeStore().delete(attribute);
            }
        }
        return true;
    }

    @Override
    public boolean removeAllAttributes() {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.removeAttributes - ");
        if (!this.isInitializedAndEnabled()) {
            return false;
        }
        this.agentConfiguration.getAnalyticAttributeStore().clear();
        this.userAttributes.clear();
        return false;
    }

    @Override
    public boolean addEvent(final String name, final Set<AnalyticAttribute> eventAttributes) {
        return this.addEvent(name, AnalyticsEventCategory.Custom, "Mobile", eventAttributes);
    }

    @Override
    public boolean addEvent(final String name, final AnalyticsEventCategory eventCategory, final String eventType, final Set<AnalyticAttribute> eventAttributes) {
        AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.addEvent - " + name + ": category=" + eventCategory + ", eventType: " + eventType + ", eventAttributes:" + eventAttributes);
        if (!this.isInitializedAndEnabled()) {
            return false;
        }
        final Set<AnalyticAttribute> validatedAttributes = new HashSet<AnalyticAttribute>();
        for (final AnalyticAttribute attribute : eventAttributes) {
            if (this.isAttributeNameValid(attribute.getName())) {
                validatedAttributes.add(attribute);
            }
        }
        final AnalyticsEvent event = AnalyticsEventFactory.createEvent(name, eventCategory, eventType, validatedAttributes);
        return this.addEvent(event);
    }

    @Override
    public boolean addEvent(final AnalyticsEvent event) {
        if (!this.isInitializedAndEnabled()) {
            return false;
        }
        final Set<AnalyticAttribute> sessionAttributes = new HashSet<AnalyticAttribute>();
        final long sessionDuration = this.agentImpl.getSessionDurationMillis();
        if (0L == sessionDuration) {
            AnalyticsControllerImpl.log.error("Harvest instance is not running! Session duration will be invalid");
        }
        else {
            sessionAttributes.add(new AnalyticAttribute("timeSinceLoad", sessionDuration / 1000.0f));
            event.addAttributes(sessionAttributes);
        }
        return this.eventManager.addEvent(event);
    }

    @Override
    public int getMaxEventPoolSize() {
        return this.eventManager.getMaxEventPoolSize();
    }

    @Override
    public void setMaxEventPoolSize(final int maxSize) {
        this.eventManager.setMaxEventPoolSize(maxSize);
    }

    @Override
    public void setMaxEventBufferTime(final int maxBufferTimeInSec) {
        this.eventManager.setMaxEventBufferTime(maxBufferTimeInSec);
    }

    @Override
    public int getMaxEventBufferTime() {
        return this.eventManager.getMaxEventBufferTime();
    }

    @Override
    public EventManager getEventManager() {
        return this.eventManager;
    }

    public static AnalyticsControllerImpl getInstance() {
        return AnalyticsControllerImpl.instance;
    }

    void loadPersistentAttributes() {
        if (AnalyticsControllerImpl.log.getLevel() == 4) {
            AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.loadPersistentAttributes - loading userAttributes from the attribute store...");
        }
        final List<AnalyticAttribute> storedAttrs = this.agentConfiguration.getAnalyticAttributeStore().fetchAll();
        if (AnalyticsControllerImpl.log.getLevel() == 4) {
            AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.loadPersistentAttributes - found " + storedAttrs.size() + " userAttributes in the attribute store...");
        }
        for (final AnalyticAttribute attr : storedAttrs) {
            this.userAttributes.add(attr);
        }
    }

    private AnalyticAttribute getSystemAttribute(final String name) {
        AnalyticAttribute attribute = null;
        for (final AnalyticAttribute nextAttribute : this.systemAttributes) {
            if (nextAttribute.getName().equals(name)) {
                attribute = nextAttribute;
                break;
            }
        }
        return attribute;
    }

    private AnalyticAttribute getUserAttribute(final String name) {
        AnalyticAttribute attribute = null;
        for (final AnalyticAttribute nextAttribute : this.userAttributes) {
            if (nextAttribute.getName().equals(name)) {
                attribute = nextAttribute;
                break;
            }
        }
        return attribute;
    }

    private void clear() {
        if (AnalyticsControllerImpl.log.getLevel() == 4) {
            AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.clear - clearing out attributes and events");
        }
        this.systemAttributes.clear();
        this.userAttributes.clear();
        this.eventManager.empty();
    }

    private boolean isAttributeNameValid(final String name) {
        boolean valid = this.isNameValid(name);
        if (valid) {
            valid = !this.isNameReserved(name);
            if (!valid) {
                AnalyticsControllerImpl.log.error("Attribute name " + name + " is reserved for internal use and will be ignored.");
            }
        }
        return valid;
    }

    private boolean isNameValid(final String name) {
        final boolean valid = name != null && !name.equals("") && name.length() < 256;
        if (!valid) {
            AnalyticsControllerImpl.log.error("Attribute name " + name + " is null, empty, or exceeds the maximum length of " + 256 + " characters.");
        }
        return valid;
    }

    private boolean isStringValueValid(final String name, final String value) {
        final boolean valid = value != null && !value.equals("") && value.getBytes().length < 4096;
        if (!valid) {
            AnalyticsControllerImpl.log.error("Attribute value for name " + name + " is null, empty, or exceeds the maximum length of " + 4096 + " bytes.");
        }
        return valid;
    }

    private boolean isNameReserved(final String name) {
        boolean isReserved = AnalyticsControllerImpl.reservedNames.contains(name);
        if (isReserved) {
            if (AnalyticsControllerImpl.log.getLevel() == 4) {
                AnalyticsControllerImpl.log.verbose("Name " + name + " is in the reserved names list.");
            }
            return isReserved;
        }
        isReserved = (isReserved || name.startsWith("newRelic"));
        if (isReserved) {
            if (AnalyticsControllerImpl.log.getLevel() == 4) {
                AnalyticsControllerImpl.log.verbose("Name " + name + " starts with reserved prefix " + "newRelic");
            }
            return isReserved;
        }
        isReserved = (isReserved || name.startsWith("nr."));
        if (isReserved && AnalyticsControllerImpl.log.getLevel() == 4) {
            AnalyticsControllerImpl.log.verbose("Name " + name + " starts with reserved prefix " + "nr.");
        }
        return isReserved;
    }

    @Override
    public boolean recordEvent(final String name, final Map<String, Object> eventAttributes) {
        if (AnalyticsControllerImpl.log.getLevel() == 4) {
            AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.recordEvent - " + name + ": " + eventAttributes.size() + " attributes");
        }
        if (!this.isInitializedAndEnabled()) {
            return false;
        }
        final Set<AnalyticAttribute> attributes = new HashSet<AnalyticAttribute>();
        try {
            for (final String key : eventAttributes.keySet()) {
                final Object value = eventAttributes.get(key);
                try {
                    if (value instanceof String) {
                        attributes.add(new AnalyticAttribute(key, String.valueOf(value)));
                    }
                    else if (value instanceof Float) {
                        attributes.add(new AnalyticAttribute(key, Float.valueOf((float)value)));
                    }
                    else if (value instanceof Double) {
                        attributes.add(new AnalyticAttribute(key, Float.valueOf((float)value)));
                    }
                    else if (value instanceof Integer) {
                        attributes.add(new AnalyticAttribute(key, Float.valueOf((int)value)));
                    }
                    else if (value instanceof Short) {
                        attributes.add(new AnalyticAttribute(key, Float.valueOf((short)value)));
                    }
                    else if (value instanceof Long) {
                        attributes.add(new AnalyticAttribute(key, Float.valueOf((long)value)));
                    }
                    else if (value instanceof BigDecimal) {
                        attributes.add(new AnalyticAttribute(key, Float.valueOf(((BigDecimal)value).floatValue())));
                    }
                    else if (value instanceof BigInteger) {
                        attributes.add(new AnalyticAttribute(key, Float.valueOf(((BigInteger)value).floatValue())));
                    }
                    else {
                        if (!(value instanceof Boolean)) {
                            AnalyticsControllerImpl.log.error("Unsupported event attribute type for key [" + key + "]: " + value.getClass().getName());
                            return false;
                        }
                        attributes.add(new AnalyticAttribute(key, Boolean.valueOf((boolean)value)));
                    }
                }
                catch (ClassCastException e) {
                    AnalyticsControllerImpl.log.error(String.format("Error casting attribute [%s] to String or Float: ", key), e);
                }
            }
        }
        catch (Exception e2) {
            AnalyticsControllerImpl.log.error(String.format("Error occurred while recording event [%s]: ", name), e2);
        }
        return this.addEvent(name, AnalyticsEventCategory.Custom, "Mobile", attributes);
    }

    private boolean isInitializedAndEnabled() {
        if (!AnalyticsControllerImpl.initialized.get()) {
            AnalyticsControllerImpl.log.warning("Analytics controller is not initialized!");
            return false;
        }
        if (!this.isEnabled) {
            AnalyticsControllerImpl.log.warning("Analytics controller is not enabled!");
            return false;
        }
        return true;
    }

    static {
        log = AgentLogManager.getAgentLog();
        instance = new AnalyticsControllerImpl();
        initialized = new AtomicBoolean(false);
        reservedNames = new ArrayList<String>();
    }

    class InteractionCompleteListener implements TraceLifecycleAware
    {
        @Override
        public void onEnterMethod() {
        }

        @Override
        public void onExitMethod() {
        }

        @Override
        public void onTraceStart(final ActivityTrace activityTrace) {
        }

        @Override
        public void onTraceComplete(final ActivityTrace activityTrace) {
            AnalyticsControllerImpl.log.verbose("AnalyticsControllerImpl.InteractionCompleteListener.onTraceComplete invoke.");
            final AnalyticsEvent event = this.createTraceEvent(activityTrace);
            final AnalyticsController analyticsController = AnalyticsControllerImpl.getInstance();
            analyticsController.addEvent(event);
        }

        private AnalyticsEvent createTraceEvent(final ActivityTrace activityTrace) {
            final float durationInSec = activityTrace.rootTrace.getDurationAsSeconds();
            final Set<AnalyticAttribute> attrs = new HashSet<AnalyticAttribute>();
            attrs.add(new AnalyticAttribute("interactionDuration", durationInSec));
            return AnalyticsEventFactory.createEvent(activityTrace.rootTrace.displayName, AnalyticsEventCategory.Interaction, "Mobile", attrs);
        }
    }



}
