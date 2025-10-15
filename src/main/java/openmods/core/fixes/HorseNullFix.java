package openmods.core.fixes;

import openmods.api.IResultListener;
import openmods.asm.MethodMatcher;
import openmods.asm.VisitorHelper;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public abstract class HorseNullFix extends ClassVisitor {

	public static class Base extends HorseNullFix {
		public Base(String horseClass, ClassVisitor cv, IResultListener listener) {
			super(horseClass, cv, listener);
		}

		private static boolean hasFixed;

		public static boolean isWorking() {
			return hasFixed;
		}

		@Override
		protected void setFixFlag() {
			hasFixed = true;
		}
	}

	public static class Llama extends HorseNullFix {
		public Llama(String horseClass, ClassVisitor cv, IResultListener listener) {
			super(horseClass, cv, listener);
		}

		private static boolean hasFixed;

		public static boolean isWorking() {
			return hasFixed;
		}

		@Override
		protected void setFixFlag() {
			hasFixed = true;
		}
	}

	public static class Horse extends HorseNullFix {
		public Horse(String horseClass, ClassVisitor cv, IResultListener listener) {
			super(horseClass, cv, listener);
		}

		private static boolean hasFixed;

		public static boolean isWorking() {
			return hasFixed;
		}

		@Override
		protected void setFixFlag() {
			hasFixed = true;
		}
	}

	private final IResultListener listener;

	private final MethodMatcher modifiedMethod;

	public HorseNullFix(String horseClass, ClassVisitor cv, IResultListener listener) {
		super(Opcodes.ASM5, cv);
		this.listener = listener;

		modifiedMethod = new MethodMatcher(horseClass, Type.getMethodType(Type.VOID_TYPE).getDescriptor(), "updateHorseSlots", "func_110232_cE");
	}

	protected abstract void setFixFlag();

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor parent = super.visitMethod(access, name, desc, signature, exceptions);
		return modifiedMethod.match(name, desc)? new FixerMethodVisitor(parent) : parent;
	}

	private class FixerMethodVisitor extends MethodVisitor {

		public FixerMethodVisitor(MethodVisitor mv) {
			super(Opcodes.ASM5, mv);
		}

		@Override
		public void visitCode() {
			super.visitCode();

			final String worldObjName = VisitorHelper.useSrgNames()? "field_70170_p" : "world";
			final String entityClsName = "net/minecraft/entity/Entity";
			final String worldCls = Type.getObjectType("net/minecraft/world/World").getDescriptor();

			visitVarInsn(Opcodes.ALOAD, 0); // this
			visitFieldInsn(Opcodes.GETFIELD, entityClsName, worldObjName, worldCls);

			final Label skip = new Label();
			visitJumpInsn(Opcodes.IFNONNULL, skip);
			visitInsn(Opcodes.RETURN);
			visitLabel(skip);

			listener.onSuccess();
			setFixFlag();
		}
	}

}
