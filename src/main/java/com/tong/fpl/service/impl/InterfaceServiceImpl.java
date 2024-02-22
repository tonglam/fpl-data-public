package com.tong.fpl.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.letletme.notify.NotifyData;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.utils.HttpUtils;
import com.tong.fpl.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2021/8/30
 */
@Service
public class InterfaceServiceImpl implements IInterfaceService {

    @Override
    public void telegramText(String text, List<String> userList) {
        try {
            NotifyData data = new NotifyData()
                    .setUserList(userList)
                    .setText(text)
                    .setImgCaption("")
                    .setImgUrl("");
            HttpUtils.httpPost(Constant.NOTIFICATION_TEXT, JsonUtils.obj2json(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void telegramImage(String url, String caption, List<String> userList) {
        try {
            NotifyData data = new NotifyData()
                    .setUserList(userList)
                    .setText("")
                    .setImgCaption(caption)
                    .setImgUrl(url);
            HttpUtils.httpPost(Constant.NOTIFICATION_TEXT, JsonUtils.obj2json(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<StaticRes> getBootstrapStatic() {
        try {
            String result = HttpUtils.httpGet(Constant.BOOTSTRAP_STATIC).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, StaticRes.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<EventFixturesRes>> getEventFixture(int event) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.EVENT_FIXTURES, event)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, new TypeReference<List<EventFixturesRes>>() {
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<EntryRes> getEntry(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.ENTRY, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, EntryRes.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<EntryCupRes> getEntryCup(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.ENTRY_CUP, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, EntryCupRes.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserHistoryRes> getUserHistory(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.USER_HISTORY, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            UserHistoryRes userHistoryRes = mapper.readValue(result, UserHistoryRes.class);
            userHistoryRes.setEntry(entry);
            return Optional.of(userHistoryRes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserPicksRes> getUserPicks(int event, int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.USER_PICKS, entry, event)).orElse("");
            if (StringUtils.isBlank(result) || result.contains("Not found")) {
                return Optional.empty();
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, UserPicksRes.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<UserTransfersRes>> getUserTransfers(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.USER_TRANSFER, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, new TypeReference<List<UserTransfersRes>>() {
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<EventLiveRes> getEventLive(int event) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.EVENT_LIVE, event)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, EventLiveRes.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<ElementSummaryRes> getElementSummary(int element) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.ELEMENT_SUMMARY, element)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, ElementSummaryRes.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_CLASSIC, classicId, page)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueClassicRes.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<LeagueH2hRes> getLeagueH2H(int h2hId, int page) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_H2H, h2hId, page)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueH2hRes.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Integer> getEntryListFromClassicByLimit(int classicId, int limit) {
        int endPage = this.getEndPage(limit);
        return this.getOnePageEntryListFromClassic(Lists.newArrayList(), classicId, 1, endPage);
    }

    @Override
    public List<Integer> getEntryListFromH2hByLimit(int h2hId, int limit) {
        int endPage = this.getEndPage(limit);
        return this.getOnePageEntryListFromH2h(Lists.newArrayList(), h2hId, 1, endPage);
    }

    private int getEndPage(int limit) {
        if (limit > 0 && limit <= 50) {
            return 1;
        }
        return (int) Math.ceil(limit * 1.0 / 50);
    }

    private List<Integer> getOnePageEntryListFromClassic(List<Integer> list, int classicId, int page, int endPage) {
        Optional<LeagueClassicRes> resResult = this.getLeaguesClassic(classicId, page);
        if (resResult.isPresent()) {
            LeagueClassicRes leagueClassicRes = resResult.get();
            if (!CollectionUtils.isEmpty(leagueClassicRes.getStandings().getResults())) {
                List<Integer> newList = Lists.newArrayList();
                leagueClassicRes.getStandings().getResults().forEach(o -> newList.add(o.getEntry()));
                if (CollectionUtils.isEmpty(list)) {
                    list = newList;
                } else {
                    list.addAll(newList);
                }
            }
            if (leagueClassicRes.getStandings().isHasNext()) {
                page++;
                if (endPage > 0 && page > endPage) {
                    return list;
                }
                this.getOnePageEntryListFromClassic(list, classicId, page, endPage);
            }
        }
        return list;
    }

    private List<Integer> getOnePageEntryListFromH2h(List<Integer> list, int h2hId, int page, int endPage) {
        Optional<LeagueH2hRes> resResult = this.getLeagueH2H(h2hId, page);
        if (resResult.isPresent()) {
            LeagueH2hRes leagueH2hRes = resResult.get();
            if (!CollectionUtils.isEmpty(leagueH2hRes.getStandings().getResults())) {
                List<Integer> newList = Lists.newArrayList();
                leagueH2hRes.getStandings().getResults().forEach(o -> newList.add(o.getEntry()));
                if (CollectionUtils.isEmpty(list)) {
                    list = newList;
                } else {
                    list.addAll(newList);
                }
                if (leagueH2hRes.getStandings().isHasNext()) {
                    page++;
                    if (endPage > 0 && page > endPage) {
                        return list;
                    }
                    this.getOnePageEntryListFromH2h(list, h2hId, page, endPage);
                }
            }
        }
        return list;
    }

}
