#!/usr/bin/env bash
mkdir -p out/production/OracleDatabaseUser 2> /dev/null
javac -d out/production/OracleDatabaseUser -cp src/:ojdbc14.jar src/br/usp/icmc/OracleManager/*.java
cp -Rf src/br/usp/icmc/OracleManager/*.fxml out/production/OracleDatabaseUser/br/usp/icmc/OracleManager/.
