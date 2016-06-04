package joelbits.emu.cpu.registers;

public final class InstructionRegister<T> implements Register<T> {
	private T instructionRegister;

	@Override
	public T read() {
		return instructionRegister;
	}

	@Override
	public void write(T data) {
		instructionRegister = data;
	}
}
