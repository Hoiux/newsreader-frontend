package org.hoiux.newsreader.views.main;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.PermitAll;
import org.hoiux.newsreader.data.Category;
import org.hoiux.newsreader.data.Channel;
import org.hoiux.newsreader.data.FeedCollection;
import org.hoiux.newsreader.data.Item;
import org.hoiux.newsreader.services.BackendService;
import org.hoiux.newsreader.services.SecurityService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@PageTitle("Main")
@Route(value = "")
@PermitAll
@Uses(Icon.class)
public class MainView extends Composite<VerticalLayout> {

    BackendService backend;
    FeedCollection feedCollection;
    VerticalLayout newsItems = new VerticalLayout();

    public MainView(BackendService backend, SecurityService securityService) {

        this.backend = backend;
        this.feedCollection = backend.getChannels();

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        // Set up whole web page.
        HorizontalLayout wholePage = new HorizontalLayout();
        //wholePage.getStyle().set("border", "1px solid grey");
        wholePage.setHeight("100%");
        wholePage.setWidth("100%");
        wholePage.getStyle().set("flex-grow", "1");
        wholePage.addClassName(Gap.SMALL);
        getContent().add(wholePage);

        // Set up a fixed sidebar containing the list of channels and the filter strings box.

        VerticalLayout sidebar = new VerticalLayout(/*new DrawerToggle()*/);
        sidebar.setClassName("sidebar");
        sidebar.getStyle().set("border", "1px solid gainsboro");
        sidebar.setWidth(450, Unit.PIXELS);
        sidebar.setHeight("100%");
        sidebar.getStyle().set("flex-grow", "1");

        // Set up the layout for list of channels inside the sidebar.
        VerticalLayout channelsColumn = new VerticalLayout();
        channelsColumn.setClassName("feedList");
        //channelsColumn.getStyle().set("border", "1px solid red");
        channelsColumn.setWidthFull();
        channelsColumn.getStyle().set("flex-grow", "1");

        // Add the list of channels to the sidebar.
        H4 channelsTitle = new H4("Feeds");
        channelsTitle.setWidth("max-content");
        sidebar.add(channelsTitle);
        sidebar.setFlexGrow(1.0, channelsColumn);

        fillChannelTree(channelsColumn);

        sidebar.add(channelsColumn);

        // Add new feed field and button
        TextField textField = new TextField();
        textField.setPlaceholder("Click to add a new RSS feed...");
        Button button = new Button("Add Feed", event -> {
            this.backend.addNewFeed(textField.getValue());
            UI.getCurrent().getPage().reload();
        });

        HorizontalLayout hz = new HorizontalLayout(textField, button);
        channelsColumn.add(hz);

        // Set up the Filters box...
        VerticalLayout filtersColumn = new VerticalLayout();
        createFiltersLayout(filtersColumn, this.backend);

        sidebar.add(new Hr());

        H4 filterTitle = new H4("Filters");
        filterTitle.setWidth("max-content");

        sidebar.add(filterTitle);
        sidebar.add(filtersColumn);

        // Sidebar is ready...add it to the page.
        wholePage.add(sidebar);

        // Set up the right hand layout.

        // Create the Logout button.
        String u = securityService.getAuthenticatedUser().getUsername();
        Button logout = new Button("Log out " + u,
                e -> securityService.logout());

        Button refresh = new Button("Refresh News",
                e -> fillNewsItems(newsItems, backend.refreshAllFeeds().getFeeds()));

        H1 logo = new H1("News Items");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);

        H1 padding = new H1();
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);

        // Create a header that will contain the H1 and logout button.
        HorizontalLayout header = new HorizontalLayout(logo, refresh, padding, logout);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(padding);

        // Set up the main layout.
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.getStyle().set("flex-grow", "1");
        mainLayout.getStyle().set("border", "1px solid gainsboro");

        mainLayout.add(header);

        // Set up the actual container for the list of news items.
        newsItems.setWidthFull();
        newsItems.getStyle().set("flex-grow", "1");
        //newsItems.getStyle().set("border", "1px solid green");

        fillNewsItems(newsItems, backend.getChannels().getFeeds());

        mainLayout.add(newsItems);
        mainLayout.setFlexGrow(1.0, newsItems);

        // Main layout is ready...add it to the web page.
        wholePage.add(mainLayout);
    }

    private static void createFiltersLayout(VerticalLayout filtersColumn, BackendService backend) {
        //filtersColumn.getStyle().set("border", "1px solid purple");
        filtersColumn.setWidthFull();
        filtersColumn.getStyle().set("flex-grow", "1");

        String filters = backend.getFilters();

        TextArea filterStrings = new TextArea();
        filterStrings.setWidth("100%");

        if (filters.isBlank()) {
            filterStrings.setPlaceholder("Enter filters here, one per line...");
        } else {
            filterStrings.setValue(filters);
        }

        filtersColumn.add(filterStrings);

        Button button = new Button("Apply", e -> {
            backend.applyFilters(filterStrings.getValue());
            UI.getCurrent().getPage().reload();
        });

        filtersColumn.add(button);
    }

    private void fillNewsItems(VerticalLayout newsItems, List<Channel> channels) {

        // got lots of this from here: https://vaadin.com/docs/latest/components/message-list

        newsItems.removeAll();

        MessageList messageList = new MessageList();

        List<MessageListItem> messageListItems = new ArrayList<>();

        for (Channel channel : channels) {

            for (Item item : channel.getItems()) {

                if (item.getIsVisible()) { // hide filtered items from UI

                    MessageListItem messageListItem = new MessageListItem();

                    messageListItem.setUserName(item.getTitle());
                    messageListItem.setUserImage(channel.getImage());
                    messageListItem.setText(/*item.getDescription()*/item.getLink());

                    // from here: https://www.baeldung.com/java-string-to-instant
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss", Locale.UK);
                    LocalDateTime localDateTime = LocalDateTime.parse(item.getPubDate(), dateTimeFormatter);
                    ZoneId zoneId = ZoneId.of("Europe/London");
                    ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
                    Instant instant = zonedDateTime.toInstant();
                    messageListItem.setTime(instant);

                    messageListItems.add(messageListItem);
                }
            }
        }

        messageList.setItems(messageListItems);
        newsItems.add(messageList);
    }

    private void fillChannelTree(VerticalLayout channelsColumn) {

        // got lots of this from here: https://vaadin.com/docs/latest/components/details

        List<Category> categories = backend.getCategories();

        // Put each channel under its category. Channels without categories go at the end.
        List<Channel> channelSet = feedCollection.getFeeds();

        for (Category category : categories) {

            Details d = new Details();
            d.addThemeVariants(DetailsVariant.SMALL);
            //d.addThemeVariants(DetailsVariant.FILLED);  // ugh, don't like how this looks
            d.setClassName("feedListDetails");
            d.setSummaryText(category.getName());

            for (Channel ch : channelSet) {

                if (ch.getCatId().equals(category.getId())) {

                    Span span = new Span(ch.getDescription());
                    span.setClassName("channelName");

                    // Update the news view with a single channel.
                    span.addClickListener(new ComponentEventListener<ClickEvent<Span>>() {
                        @Override
                        public void onComponentEvent(ClickEvent<Span> event) {
                            List<Channel> channelList = new ArrayList<>();
                            channelList.add(ch);
                            fillNewsItems(newsItems, channelList);
                        }
                    });

                    span.setClassName("feedListChannel");
                    d.add(span);
                }
            }

            channelsColumn.add(d);
        }

        // Add channels without categories
        Details d = new Details();
        d.setClassName("feedListDetails");
        d.addThemeVariants(DetailsVariant.SMALL);
        //d.addThemeVariants(DetailsVariant.FILLED);
        d.setSummaryText("No Category");

        for (Channel ch : channelSet) {
            if (ch.getCatId() == -1L) {
                Span span = new Span(ch.getDescription());

                // Update the news view with a single channel.
                span.addClickListener(new ComponentEventListener<ClickEvent<Span>>() {
                    @Override
                    public void onComponentEvent(ClickEvent<Span> event) {
                        List<Channel> channelList = new ArrayList<>();
                        channelList.add(ch);
                        fillNewsItems(newsItems, channelList);
                    }
                });

                span.setClassName("feedListChannel");
                d.add(span);
            }
        }

        channelsColumn.add(d);
    }

}
