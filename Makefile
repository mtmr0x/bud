jar:
	jar cf target/bud.jar -C src .

watch:
	 npx shadow-cljs watch app
