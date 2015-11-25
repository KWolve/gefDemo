package wei.learn.gef.policy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import wei.learn.gef.command.CreateConnectionCommand;
import wei.learn.gef.command.ReconnectConnectionCommand;
//ide�ж�Ӧ�ࣺ GraphicalNodePolicy
public class CustomGraphicalNodeEditPolicy extends GraphicalNodeEditPolicy
{

    @Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request)
	{
	    CreateConnectionCommand command = new CreateConnectionCommand();
	    command.setConnection(request.getNewObject());
	    command.setSource(getHost().getModel());
	    request.setStartCommand(command);
	    return command;
	}

	@Override
    protected Command getConnectionCompleteCommand(CreateConnectionRequest request)
    {
        CreateConnectionCommand command =(CreateConnectionCommand) request.getStartCommand();
        command.setTarget(getHost().getModel());
        return command;
    }


    @Override
    protected Command getReconnectSourceCommand(ReconnectRequest request)
    {
        ReconnectConnectionCommand command = new ReconnectConnectionCommand();
        command.setConnectionModel(request.getConnectionEditPart().getModel());
        command.setNewSource(getHost().getModel());
        return command;
    }

    @Override
    protected Command getReconnectTargetCommand(ReconnectRequest request)
    {
        ReconnectConnectionCommand command = new ReconnectConnectionCommand();
        command.setConnectionModel(request.getConnectionEditPart().getModel());
        command.setNewTarget(getHost().getModel());
        return command;
    }

}
