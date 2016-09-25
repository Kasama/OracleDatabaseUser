#!/usr/bin/env bash
javac -d out/production/OracleDatabase -cp src/:ojdbc14.jar src/br/usp/icmc/OracleManager/*.java
cp -Rf src/br/usp/icmc/OracleManager/*.fxml out/production/OracleDatabase/br/usp/icmc/OracleManager/.