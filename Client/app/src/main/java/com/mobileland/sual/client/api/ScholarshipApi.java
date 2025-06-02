package com.mobileland.sual.client.api;

import com.mobileland.sual.client.Notice;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ScholarshipApi {
    @GET("api/notices/scholarship")
    Call<List<Notice>> getScholarshipNotices();
}