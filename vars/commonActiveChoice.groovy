def call(Map config) {
             [$class      : "${config.class}",
             choiceType  : "${config.choiceType}",
             description : config.description != null ? config.description : config.name,
             filterLength: config.filterLength != null ? config.filterLength : 1,
             filterable  : config.filterable != null ? config.filterable : false,
             name        : "${config.name}",
             randomName  : '',
             script      : [
                     $class        : 'GroovyScript',
                     fallbackScript: [
                             classpath: [],
                             sandbox  : config.fallbackScriptSandbox != null ? config.fallbackScriptSandbox : false,
                             script   : "${config.fallbackScript}"
                     ],
                     script        : [
                             classpath: [],
                             sandbox  : config.scriptSandbox != null ? config.scriptSandbox : false,
                             script   : "${config.script}"
                     ]
             ]
             ]

}
