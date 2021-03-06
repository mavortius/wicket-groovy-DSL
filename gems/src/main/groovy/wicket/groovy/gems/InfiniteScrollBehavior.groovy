package wicket.groovy.gems
import groovy.transform.CompileStatic
import org.apache.wicket.Component
import org.apache.wicket.MarkupContainer
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.head.IHeaderResponse
import org.apache.wicket.markup.head.OnDomReadyHeaderItem
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
/**
 * AjaxBehavior for infinite scrolling
 *
 * @author @euegnekamenev
 * @param < T >
 */
@CompileStatic
class InfiniteScrollBehavior<T> extends AbstractDefaultAjaxBehavior {
    /**
     * Closure that returns List<T>, for adding elements
     */
    Closure<List<T>> list

    /**
     * Script will be invoked onDomReady event
     */
    String headerScript

    /**
     * Script that appends to AjaxRequestTarget, when request completes
     */
    private String appendScript

    /**
     * Default markup element
     */
    String element = 'div'

    /**
     * Ajax respond event
     * @param target
     */
    @Override
    protected void respond(final AjaxRequestTarget target) {
        def scroll = (this.component) as InfiniteScroll
        def markupId = component.markupId
        def script = ''
        appendScript = null
        def markupIds = []
        this.list = this.list?.rehydrate(null, null, this.list.thisObject)
        this.list?.call(scroll.pageId.toInteger())?.each { Serializable obj ->
            def item = new ListItem(++scroll.rows.size(), obj.toLoadModel())
            markupIds << "#$item.markupId"
            scroll.rowBuild(item)
            script += "var item=document.createElement('$element');" +
                    "item.id='$item.markupId';" +
                    "Wicket.\$('$scroll.content.markupId').appendChild(item);"
            target.add(item)
        }
        script += "\$('#scrollingUpdate').remove();"
        target.prependJavaScript(script);
        if (markupIds.size() > 0) {
            appendScript = """\$('#$markupId').DataTable().rows.add(\$('${
                markupIds.join(',')
            }')).draw(false);"""
        }
        appendScript ? target.appendJavaScript(appendScript) : null
    }
    /**
     * Render ajax response header
     *
     * @param component
     * @param response
     */
    @Override
    void renderHead(final Component component, final IHeaderResponse response) {
        super.renderHead(component, response)
        def markupId = component.markupId
        def script = """
            var scroller = \$('#${(component as InfiniteScroll).content.markupId}').parent().parent();
                scroller.scroll(function() {
            if ((scroller.scrollTop() === (scroller[0].scrollHeight - scroller[0].clientHeight)) && scroller[0].scrollHeight >= scroller[0].clientHeight) {
                 if(\$('#scrollingUpdate').size() <= 0) {
                    var item = document.createElement('tr');
                    item.id = 'scrollingUpdate';
                    \$(item).append('<td colspan="42"><div class="ui center inverted aligned segment" style="min-height: 150px"><i class="huge loading icon"></i></div></td>');
                    \$('#${(component as InfiniteScroll).content.markupId}').append(\$(item));
                    ${callbackScript}
                 }
            }
        }).scroll();
        """
        headerScript ? headerScript += script : null
        response.render(OnDomReadyHeaderItem.forScript(headerScript));
    }
}

/**
 * Interface for safe InfiniteScroll implementations
 */
@CompileStatic
interface InfiniteScroll {
    MarkupContainer getContent()

    ListView getRows()

    ListItem rowBuild(ListItem item)

    Long getPageId()
}
