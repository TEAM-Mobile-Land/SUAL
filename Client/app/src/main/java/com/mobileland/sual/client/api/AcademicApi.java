package com.mobileland.sual.client.api;

import com.mobileland.sual.client.Notice;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface AcademicApi {
    @GET("api/notices/academic")
    Call<List<Notice>> getAcademicNotices();
}