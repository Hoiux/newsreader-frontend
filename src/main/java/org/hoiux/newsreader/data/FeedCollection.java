package org.hoiux.newsreader.data;

import java.util.List;

public class FeedCollection {

    List<Channel> feeds;

    public FeedCollection() {
    }

    public List<Channel> getFeeds() {
        return feeds;
    }
    public void setFeeds(List<Channel> feeds) {
        this.feeds = feeds;
    }

}
