package hudson.plugins.jira;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.tasks.Notifier;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rupali
 * Date: 13/12/12
 * Time: 7:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class JiraCreateIssueNotifier extends Notifier{

    private String projectKey;

    public JiraCreateIssueNotifier(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {


        AbstractProject<?, ?> context = Stapler.getCurrentRequest().findAncestorObject(AbstractProject.class);
        if (Result.FAILURE==build.getResult() )  {
            try{
            createJiraIssue();
            }catch(ServiceException exp)  {
                System.out.print("Service Exception");
            }
        }

        return true;
    }

    public void createJiraIssue() throws ServiceException,IOException{
        AbstractProject<?, ?> context = Stapler.getCurrentRequest().findAncestorObject(AbstractProject.class);

        JiraSite site = JiraSite.get(context);
        if (site==null)  throw new IllegalStateException("JIRA site needs to be configured in the project "+context.getFullDisplayName());

        JiraSession session = site.createSession();
        if (session==null)  throw new IllegalStateException("Remote SOAP access for JIRA isn't configured in Jenkins");
        session.createIssue(projectKey);

    }

    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(JiraCreateIssueNotifier.class);
        }

        @Override
        public JiraCreateIssueNotifier newInstance(StaplerRequest req,
                                             JSONObject formData) throws FormException {
            return req.bindJSON(JiraCreateIssueNotifier.class, formData);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "JiraCreateIssue" ;
        }

        @Override
        public String getHelpFile() {
            return "/plugin/jira/help-jira-create-issue.html";
        }
    }
}
