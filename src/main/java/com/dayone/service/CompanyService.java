package com.dayone.service;

import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.exception.ScrapException;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.dayone.type.ErrorCode.COMPANY_ALREADY_SAVED;
import static com.dayone.type.ErrorCode.COMPANY_NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;

    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        log.info("Saving company with ticker: {}", ticker);
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new ScrapException(COMPANY_ALREADY_SAVED, BAD_REQUEST);
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        log.info("Getting all companies with page: {}", pageable);
        Page<CompanyEntity> companies = this.companyRepository.findAll(pageable);
        return companies;
    }

    private Company storeCompanyAndDividend(String ticker) {
        log.info("Storing company and dividend information for ticker: {}", ticker);
        // 1. ticker 를 기준으로 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new ScrapException(COMPANY_NOT_FOUND, BAD_REQUEST);
        }

        // 2. 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 3. 스크래핑 결과 반환
        CompanyEntity companyEntity =
                this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntityList =
                scrapedResult.getDividends().stream()
                        .map(e -> new DividendEntity(companyEntity.getId(), e))
                        .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntityList);

        return company;
    }

    public List<String> getCompanyNamesByKeyword(String keyword) {
        log.info("Getting company names by keyword: {}", keyword);
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities =
                this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);

        List<String> companyNames = companyEntities.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());

        return companyNames;
    }

    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null);
    }

    public List<String> autocomplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream()
                .collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        log.info("Deleting company with ticker: {}", ticker);
        // 1. 배당금 정보 삭제
        var company = this.companyRepository.findByTicker(ticker)
                .orElseThrow(() -> new ScrapException(COMPANY_NOT_FOUND, BAD_REQUEST));

        // 2. 회사 정보 삭제
        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        // 트라이에 있는 데이터도 삭제
        this.deleteAutocompleteKeyword(company.getName());

        return company.getName();
    }

}
