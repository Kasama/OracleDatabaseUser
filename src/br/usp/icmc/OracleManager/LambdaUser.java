package br.usp.icmc.OracleManager;

@FunctionalInterface
public interface LambdaUser<T> {
	void use(T element);
}
