package test.web

import groovy.transform.CompileStatic

@CompileStatic
class FormsPage extends TemplatePage {
    private Integer sum = 0
    private Integer first
    private Integer second

    @Override
    protected void onInitialize() {
        super.onInitialize()
        form('simpleForm') {
            compoundModel(this)
            text('first')
            text('second')
            label('sum')
            submit {
                this.sum = this.first + this.second
            }
        }
    }
}
