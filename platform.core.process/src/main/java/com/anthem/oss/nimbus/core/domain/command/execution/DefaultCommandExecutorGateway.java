/**
 * 
 */
package com.anthem.oss.nimbus.core.domain.command.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import com.anthem.oss.nimbus.core.BeanResolverStrategy;
import com.anthem.oss.nimbus.core.domain.command.Behavior;
import com.anthem.oss.nimbus.core.domain.command.Command;
import com.anthem.oss.nimbus.core.domain.command.CommandBuilder;
import com.anthem.oss.nimbus.core.domain.command.CommandElement.Type;
import com.anthem.oss.nimbus.core.domain.command.CommandMessage;
import com.anthem.oss.nimbus.core.domain.command.execution.CommandExecution.Input;
import com.anthem.oss.nimbus.core.domain.command.execution.CommandExecution.MultiOutput;
import com.anthem.oss.nimbus.core.domain.command.execution.CommandExecution.Output;
import com.anthem.oss.nimbus.core.domain.definition.Constants;
import com.anthem.oss.nimbus.core.domain.definition.Execution;
import com.anthem.oss.nimbus.core.domain.model.state.EntityState.Param;

/**
 * @author Soham Chakravarti
 *
 */
@RefreshScope
public class DefaultCommandExecutorGateway extends BaseCommandExecutorStrategies implements CommandExecutorGateway {
	
	@SuppressWarnings("rawtypes")
	private final Map<String, CommandExecutor> executors;
	
	private ExecutionContextLoader loader;
	
	public DefaultCommandExecutorGateway(BeanResolverStrategy beanResolver) {
		super(beanResolver);
		
		this.executors = new HashMap<>();
	}
	
	@PostConstruct
	public void initDependencies() {
		this.loader = getBeanResolver().get(ExecutionContextLoader.class);
	}

	
	@Override
	public MultiOutput execute(CommandMessage cmdMsg) {
		final String inputCommandUri = cmdMsg.getCommand().getAbsoluteUri();
		
		// load execution context 
		ExecutionContext eCtx = loadExecutionContext(cmdMsg);
		
		MultiOutput mOutput = new MultiOutput(inputCommandUri, eCtx, cmdMsg.getCommand().getAction(), cmdMsg.getCommand().getBehaviors());
		
		// get execution config
		Param<?> p = findParamByCommand(eCtx);
		List<Execution.Config> execConfigs = p != null ? p.getConfig().getExecutionConfigs() : null;
		
		// if present, hand-off to each command within execution config
		if(CollectionUtils.isNotEmpty(execConfigs)) {
			executeConfig(eCtx, mOutput, execConfigs);

		} else {// otherwise, execute self
			executeSelf(eCtx, mOutput);
		}
		
		return mOutput;
	}
	

	protected void executeConfig(ExecutionContext eCtx, MultiOutput mOutput, List<Execution.Config> execConfigs) {
		final CommandMessage cmdMsg = eCtx.getCommandMessage();
		
		// for-each config
		execConfigs.stream().forEach(ec->{
			String configExecPath = ec.url();
			
			if(cmdMsg.getCommand().getRootDomainUri().indexOf(":")>0) {
				configExecPath = cmdMsg.getCommand().getRootDomainUri()+ec.url(); //TODO - TEMP - would not work below if the url had the /p/ prefix. need to revisit
			}
			// prepare config command 
			configExecPath = StringUtils.contains(ec.url(), Constants.SEPARATOR_URI_PLATFORM.code+Constants.SEPARATOR_URI.code)  // check if url has "/p/" 
										? ec.url() : eCtx.getCommandMessage().getCommand().buildAlias(Type.PlatformMarker) + configExecPath;
			
										
			Command configExecCmd = CommandBuilder.withUri(configExecPath).getCommand();
			
			// TODO decide on which commands should get the payload
			CommandMessage configCmdMsg = new CommandMessage(configExecCmd, cmdMsg.getRawPayload());
			
			// execute & add output to mOutput
			MultiOutput configOutput = execute(configCmdMsg);
			mOutput.template().add(configOutput);
			
		});	
	}
	
	protected void executeSelf(ExecutionContext eCtx, MultiOutput mOutput) {
		final CommandMessage cmdMsg = eCtx.getCommandMessage();
		final String inputCommandUri = cmdMsg.getCommand().getAbsoluteUri();
		
		// for-each behavior:
		if(CollectionUtils.isEmpty(cmdMsg.getCommand().getBehaviors())) {
			cmdMsg.getCommand().templateBehaviors().add(Behavior.$execute);
		}
		cmdMsg.getCommand().getBehaviors().stream().forEach(b->{
			
			// find command executor
			CommandExecutor<?> executor = lookupExecutor(cmdMsg.getCommand(), b);
			
			// execute command
			Input input = new Input(inputCommandUri, eCtx, cmdMsg.getCommand().getAction(), b);
			Output<?> output = executor.execute(input);			
			
			mOutput.template().add(output);
		});
	}

	protected ExecutionContext loadExecutionContext(CommandMessage cmdMsg) {
		return loader.load(cmdMsg);
	}
	
	protected CommandExecutor<?> lookupExecutor(Command cmd, Behavior b) {
		return lookupBeanOrThrowEx(CommandExecutor.class, executors, cmd.getAction(), b);
	}

}
