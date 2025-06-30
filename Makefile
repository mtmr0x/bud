build:
	clojure -T:build jar

deploy:
	clojure -X:deploy

watch:
	 npx shadow-cljs watch app
