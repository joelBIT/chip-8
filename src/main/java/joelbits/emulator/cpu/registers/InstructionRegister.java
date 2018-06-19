package joelbits.emulator.cpu.registers;

public final class InstructionRegister<T> implements Register<T> {
	private static final Register<Integer> instructionRegister = new InstructionRegister<Integer>();
	private T data;
	
	private InstructionRegister() {
		
	}

	@Override
	public T read() {
		return data;
	}

	@Override
	public void write(T data) {
		this.data = data;
	}
	
	public static final Register<Integer> getInstance() {
		return instructionRegister;
	}
}
