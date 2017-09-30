package com.bettbio.core.model;

import javax.persistence.*;

public class Country {
    /**
     * ����
     */
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * ����
     */
    @Column(name = "country_name")
    private String countryName;

    /**
     * ����
     */
    @Column(name = "country_code")
    private String countryCode;

    /**
     * ��ȡ����
     *
     * @return Id - ����
     */
    public Integer getId() {
        return id;
    }

    /**
     * ��������
     *
     * @param id ����
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * ��ȡ����
     *
     * @return country_name - ����
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * ��������
     *
     * @param countryName ����
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    /**
     * ��ȡ����
     *
     * @return country_code - ����
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * ���ô���
     *
     * @param countryCode ����
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}