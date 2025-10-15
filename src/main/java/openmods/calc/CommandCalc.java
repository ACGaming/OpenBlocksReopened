package openmods.calc;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import openmods.Log;

public class CommandCalc implements ICommand {
	protected final String name;

	private final ICommandComponent commandComponent;
	private final List<String> aliases;

	public CommandCalc(ICommandComponent parentCommandComponent, String name, String... aliases) {
		this.name = "=" + name;
		this.aliases = Arrays.asList(aliases);
		this.commandComponent = parentCommandComponent.partialyExecute(WhitespaceSplitters.fromSplitArray(name));
	}

	@Override
	public int compareTo(ICommand o) {
		return name.compareTo(o.getName());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		final HelpPrinter printer = new HelpPrinter();
		printer.push(name);
		commandComponent.help(printer);
		return printer.generate();
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		return commandComponent.getTabCompletions(WhitespaceSplitters.fromSplitArray(args));
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		final IWhitespaceSplitter splitArgs = WhitespaceSplitters.fromSplitArray(args);
		try {
			commandComponent.execute(sender, splitArgs);
		} catch (NestedCommandException e) {
			e.pushCommandName(name);
			final ITextComponent message = e.getChatComponent();
			message.getStyle().setColor(TextFormatting.RED);
			sender.sendMessage(message);
		} catch (Exception e) {
			Log.info(e, "Failed to execute command");
			final List<String> causes = Lists.newArrayList();
			Throwable current = e;
			while (current != null) {
				causes.add(Strings.nullToEmpty(current.getMessage()));
				current = current.getCause();
			}
			throw new CommandException("openmodslib.command.calc_error", Joiner.on("', caused by '").join(causes));
		}
		if (!splitArgs.isFinished()) throw new CommandException("openmodslib.command.calc_extra_args", splitArgs.getTail());
	}
}
