package playRepository;

import models.*;
import models.enumeration.ResourceType;
import models.resource.Resource;

import java.util.*;

public abstract class Commit {

    public abstract String getShortId();
    public abstract String getId();
    public abstract String getShortMessage();
    public abstract String getMessage();
    public abstract User getAuthor();
    public abstract String getAuthorName();
    public abstract String getAuthorEmail();
    public abstract Date getAuthorDate();
    public abstract TimeZone getAuthorTimezone();
    public abstract String getCommitterName();
    public abstract String getCommitterEmail();
    public abstract Date getCommitterDate();
    public abstract TimeZone getCommitterTimezone();
    public abstract int getParentCount();

    public Set<User> getWatchers(Project project) {
        Set<User> actualWatchers = new HashSet<>();

        // Add the author
        if (!getAuthor().isAnonymous()) {
            actualWatchers.add(getAuthor());
        }

        // Add every user who comments on this commit
        List<CommitComment> comments = CommitComment.find.where()
                .eq("project.id", project.id).eq("commitId", getId()).findList();
        for (CommitComment c : comments) {
            User user = User.find.byId(c.authorId);
            if (user != null) {
                actualWatchers.add(user);
            }
        }

        return Watch.findActualWatchers(actualWatchers, asResource(project));
    }

    public static Project getProjectFromResourceId(String resourceId) {
        String[] pair = resourceId.split(":");
        return Project.find.byId(Long.valueOf(pair[0]));
    }

    public static Resource getAsResource(final String resourceId) {
        return new Resource() {

            @Override
            public String getId() {
                return resourceId;
            }

            @Override
            public Project getProject() {
                return getProjectFromResourceId(resourceId);
            }

            @Override
            public ResourceType getType() {
                return ResourceType.COMMIT;
            }
        };
    }

    public Resource asResource(final Project project) {
        return new Resource() {

            @Override
            public String getId() {
                return project.id + ":" + Commit.this.getId();
            }

            @Override
            public Project getProject() {
                return project;
            }

            @Override
            public ResourceType getType() {
                return ResourceType.COMMIT;
            }
        };
    }
}
