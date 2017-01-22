# Makefile configuration
.DEFAULT_GOAL := help

clean: ## Clears environment
	@rm release/*.jar 2> /dev/null || true
	@mvn clean -q

release: clean ## Makes JAR file
	@mvn package -q
	@mv target/eos.jar release/eos.jar
	@ls -lah release/

help:
	@grep --extended-regexp '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
