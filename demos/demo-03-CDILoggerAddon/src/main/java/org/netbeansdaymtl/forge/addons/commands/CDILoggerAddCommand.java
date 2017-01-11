package org.netbeansdaymtl.forge.addons.commands;

import javax.inject.Inject;
import org.jboss.forge.addon.javaee.cdi.CDIFacet;
import org.jboss.forge.addon.parser.java.beans.ProjectOperations;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.stacks.annotations.StackConstraint;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.roaster.model.source.JavaClassSource;

@StackConstraint(CDIFacet.class)
public class CDILoggerAddCommand extends AbstractProjectCommand {

	@Inject
	private ProjectFactory projectFactory;

	@Inject
	private ProjectOperations projectOperations;

	@Inject
	@WithAttributes(label = "Logger Field Name", description = "The logger field name", required = true, defaultValue = "logger")
	private UIInput<String> named;

	@Inject
	@WithAttributes(label = "Target Class", description = "The class to inject logger", required = true, type = InputType.DROPDOWN)
	private UISelectOne<JavaResource> targetClass;

	@Inject
	@WithAttributes(label = "Logger Class", description = "The logger class", type = InputType.JAVA_CLASS_PICKER, required = true, defaultValue = "java.util.logging.Logger")
	private UIInput<String> loggerClass;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(getClass()).name("Logger: Inject")
				.category(Categories.create("Logger"));
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		UIContext uiContext = builder.getUIContext();
		targetClass.setValueChoices(projectOperations
				.getProjectClasses(getSelectedProject(uiContext)));
		UISelection<Resource<?>> selection = uiContext.getInitialSelection();
		Resource resource = selection.get();
		if (resource instanceof JavaResource) {
			targetClass.setValue((JavaResource) resource);
		}
		builder.add(named).add(targetClass).add(loggerClass);
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		String fieldName = named.getValue();
		JavaResource javaResource = targetClass.getValue();
		JavaClassSource source = javaResource.getJavaType();
		if (source.hasField(fieldName)) {
                    return Results.fail("Logger '" + fieldName+ "' could not be added. A field already exists with the same name.");
		} else {
                    source.addField().setName(fieldName)
                                    .setType(loggerClass.getValue())
                                    .addAnnotation(Inject.class.getName());
                    javaResource.setContents(source);
                    return Results.success("Logger '" + fieldName + "' successfully added!");
		}
	}

	@Override
	protected boolean isProjectRequired() {
		return true;
	}

	@Override
	protected ProjectFactory getProjectFactory() {
		return projectFactory;
	}
}
