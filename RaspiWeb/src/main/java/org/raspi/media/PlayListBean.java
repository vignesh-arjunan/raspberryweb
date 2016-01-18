package org.raspi.media;

/**
 *
 * @author vignesh
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.raspi.timer.PreferencesBean;
import org.raspi.utils.PlayListWrapper;

@Named
@ApplicationScoped
public class PlayListBean {

    private List<PlayListWrapper> playLists = new ArrayList<>();
    @Inject
    private PreferencesBean preferencesBean;
    private int selectedTabIndex = 1;

    public int getSelectedTabIndex() {
        return selectedTabIndex;
    }

    public void setSelectedTabIndex(int selectedTabIndex) {
        this.selectedTabIndex = selectedTabIndex;
    }

    public List<String> getAllIndices() {
        return IntStream.range(0, playLists.size()).mapToObj(index -> (index + 1) + "").collect(Collectors.toList());
    }

    public List<PlayListWrapper> getPlayLists() {
        return playLists;
    }

    public void clearPlayList() {
        System.out.println("Clearing PlayList");
        playLists.get(selectedTabIndex - 1).getPlayList().clear();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Play List " + getSelectedTabIndex() + " Clear"));
    }

    public void savePlayList() throws IOException {
        preferencesBean.getPreferences().setPlayLists(playLists);
        preferencesBean.savePreferences();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Play List Saved"));
    }

    @PostConstruct
    public void init() {
        playLists = preferencesBean.getPreferences().getPlayLists();
    }
}
