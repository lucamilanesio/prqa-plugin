/*
 * The MIT License
 *
 * Copyright 2013 mads.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.praqma.jenkins.plugin.prqa.notifier;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.praqma.prqa.status.PRQAComplianceStatus;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author mads
 */
public class MessageComplianceThreshold extends AbstractThreshold {

    public final Integer value;
    private static final Logger log = Logger.getLogger(MessageComplianceThreshold.class.getName());

    @DataBoundConstructor
    public MessageComplianceThreshold(final Integer value, final int thresholdLevel, final Boolean improvement) {
        super(improvement);
        this.value = value;
    }    

    @Override
    public Boolean validateImprovement(PRQAComplianceStatus lastStableValue, PRQAComplianceStatus currentValue, int thresholdLevel) {
        if(lastStableValue == null) {
            return Boolean.TRUE;
        } else {
            return currentValue.getMessageCount(thresholdLevel) <= lastStableValue.getMessageCount(thresholdLevel);
        }
    }

    @Override
    public String onUnstableMessage(PRQAComplianceStatus lastStableValue, PRQAComplianceStatus currentValue, int thresholdLevel) {
        if(improvement) {
            return Messages.PRQANotifier_MaxMessagesRequirementNotMet(currentValue.getMessageCount(thresholdLevel), lastStableValue.getMessageCount(thresholdLevel));
        } else {
            return Messages.PRQANotifier_MaxMessagesRequirementNotMet(currentValue.getMessageCount(thresholdLevel), value);
        }
    }

    @Override
    public Boolean validateThreshold(PRQAComplianceStatus currentValue, int thresholdLevel) {
        Boolean res = currentValue.getMessageCount(thresholdLevel) <= value;
        log.fine( String.format("Found %s mesages, comparing to: %s",currentValue.getMessageCount(thresholdLevel), value));        
        log.fine( String.format("ValidateThreshold returned %s",res));
        return res;
    }
    
    @Override
    public Boolean validate(PRQAComplianceStatus lastStableValue, PRQAComplianceStatus currentValue, int thresholdLevel) {
        int msgWithin = currentValue.getMessageCount(thresholdLevel);
        currentValue.setMessagesWithinThreshold(msgWithin);
        if(improvement) {
            return validateImprovement(lastStableValue, currentValue, thresholdLevel);
        } else {
            return validateThreshold(currentValue, thresholdLevel);
        }        
    }
    
    
    @Extension
    public static final class DescriptorImpl extends ThresholdSelectionDescriptor<MessageComplianceThreshold> {
        
        @Override
        public String getDisplayName() {
            return "Message Compliance Threshold";
        }
        
        public FormValidation doCheckValue(@QueryParameter String value) {
            try {
                Integer parsedValue = Integer.parseInt(value);
                if(parsedValue < 0) {
                    return FormValidation.error(Messages.PRQANotifier_WrongInteger());
                }
            } catch (NumberFormatException ex) {
                return FormValidation.error(Messages.PRQANotifier_UseNoDecimals());
            }
            return FormValidation.ok();
        }
        
        @Override
        public String getHelpFile() {
            return "/plugin/prqa-plugin/config/help-thresholds.html";
        }  
    }
}
