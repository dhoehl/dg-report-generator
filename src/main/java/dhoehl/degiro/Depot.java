/*
 * Copyright (C) 2020.  Dennis Hoehl
 * This file is part of DG report generator <https://github.com/dhoehl/dg-report-generator>.
 *
 * DG report generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DG report generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DG report generator.  If not, see <http://www.gnu.org/licenses/>.
 */

package dhoehl.degiro;

import dhoehl.csv.CsvReader;
import dhoehl.model.Asset;
import dhoehl.model.ParsingError;
import dhoehl.model.Table;
import dhoehl.model.Transaction;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class Depot {
    private Set<Transaction> mTransactions = new TreeSet<>();
    private Map<String, Asset> mAssets = new TreeMap<>();
    private int mMinYear = Integer.MAX_VALUE;
    private int mMaxYear = Integer.MIN_VALUE;
    private CurrencyUnit mCurrency;
    private TransactionParser mTransactionParser;


    public Depot(File transactions) {
        CurrencyUnit.registerCurrency("GBX", -1, 0, true);

        readTransactions(transactions);
        initAssets();
    }

    private void initAssets() {
        Map<String, Asset> assets = new HashMap<>();

        for (Transaction t : mTransactions) {
            String isin = t.getIsin();
            if (assets.get(isin) == null)
                assets.put(isin, new Asset(t.getIsin(), t.getProduct(), t.getOverall().getCurrencyUnit()));
            assets.get(isin).addTransaction(t);
            mMinYear = Math.min(t.getTimestamp().getYear(), mMinYear);
            mMaxYear = Math.max(t.getTimestamp().getYear(), mMaxYear);
            if (mCurrency == null) mCurrency = t.getOverall().getCurrencyUnit();
        }
        mAssets = assets;
    }

    private void readTransactions(File transactions) {
        Table table = CsvReader.readDegiro(transactions);
        mTransactionParser = new TransactionParser(table);
        mTransactions = mTransactionParser.getParsedTransactions();
    }

    public Map<String, Asset> getAssets() {
        return mAssets;
    }

    public Map<String, Asset> getLiquidatedAssets() {
        Map<String, Asset> assets = new HashMap<>();

        for (Map.Entry<String, Asset> asset : getAssets().entrySet()) {
            if (asset.getValue().getQuantity().equals(BigDecimal.ZERO)) assets.put(asset.getKey(), asset.getValue());
        }
        return assets;
    }

    public String getAllLiquidatedAssetsString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Asset> asset : getLiquidatedAssets().entrySet()) {
            sb.append(asset.getValue().toStringShort()).append("\n");
        }
        return sb.toString();
    }

    public List<Asset> getAllTypeAssets(Asset.Type type) {
        List<Asset> assets = new ArrayList<>();
        for (Map.Entry<String, Asset> asset : getAssets().entrySet()) {
            if (asset.getValue().getType() == type)
                assets.add(asset.getValue());
        }
        return assets;
    }

    public String getAllTypeAssetsStringShort(Asset.Type type) {
        List<Asset> assets = getAllTypeAssets(type);
        StringBuilder sb = new StringBuilder();
        for (Asset asset : assets) sb.append(asset.toStringShort()).append("\n");
        return sb.toString();
    }

    public String getAllTypeAssetsString(Asset.Type type) {
        List<Asset> assets = getAllTypeAssets(type);
        StringBuilder sb = new StringBuilder();
        for (Asset asset : assets) sb.append(asset.toString()).append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<Asset> stocks = getAllTypeAssets(Asset.Type.STOCK);
        List<Asset> knockOuts = getAllTypeAssets(Asset.Type.OTHER);
        for (Asset stock : stocks) sb.append(stock).append("\n");
        for (Asset knockOut : knockOuts) sb.append(knockOut).append("\n");
        return sb.toString();
    }

    public BigMoney getProfitLoss(Asset.Type type) {
        BigMoney profitLoss = BigMoney.zero(mCurrency);
        for (Map.Entry<String, Asset> entrySet : mAssets.entrySet()) {
            Asset asset = entrySet.getValue();
            if (asset.getType() == type && asset.getProfitLoss() != null) {
                profitLoss = profitLoss.plus(asset.getProfitLoss());
            }
        }
        return profitLoss;
    }

    public Asset getAsset(String isin) {
        return mAssets.get(isin);
    }


    public BigMoney getProfitLoss(Asset.Type type, int year) {
        BigMoney profitLoss = BigMoney.zero(mCurrency);
        for (Map.Entry<String, Asset> entrySet : mAssets.entrySet()) {
            Asset asset = entrySet.getValue();
            if (asset.getType() == type && asset.getProfitLoss(year) != null) {
                profitLoss = profitLoss.plus(asset.getProfitLoss(year));
            }
        }
        return profitLoss;
    }

    public long getTradeCount(Asset.Type type, Transaction.Type transactionType) {
        long tradeCount = 0;
        for (Asset asset : getAssets().values()) {
            if (asset.getType() == type) tradeCount += asset.getTradeCount(transactionType);
        }
        return tradeCount;
    }

    public long getTradeCount(Asset.Type type, Transaction.Type transactionType, int year) {
        long tradeCount = 0;
        for (Asset asset : getAssets().values()) {
            if (asset.getType() == type && asset.getTransactions(year) != null && asset.getTransactions(year).size() > 0)
                tradeCount += asset.getTradeCount(transactionType, year);
        }
        return tradeCount;
    }


    private BigMoney getPaidFees(Asset.Type type) {
        BigMoney fees = BigMoney.zero(mCurrency);
        for (Asset asset : getAssets().values()) {
            if (asset.getType() == type)
                fees = fees.plus(asset.getPaidFees());
        }
        return fees;
    }

    private BigMoney getPaidFees(Asset.Type type, int year) {
        BigMoney fees = BigMoney.zero(mCurrency);
        for (Asset asset : getAssets().values()) {
            if (asset.getType() == type && asset.getTransactions(year) != null && asset.getTransactions(year).size() > 0)
                fees = fees.plus(asset.getPaidFees());
        }
        return fees;
    }

    private BigMoney getPaidExchangeFees(Asset.Type type) {
        BigMoney fees = BigMoney.zero(mCurrency);
        for (Asset asset : getAssets().values()) {
            if (asset.getType() == type)
                fees = fees.plus(asset.getPaidExchangeFees());
        }
        return fees;
    }

    private BigMoney getPaidExchangeFees(Asset.Type type, int year) {
        BigMoney fees = BigMoney.zero(mCurrency);
        for (Asset asset : getAssets().values()) {
            if (asset.getType() == type && asset.getTransactions(year) != null && asset.getTransactions(year).size() > 0)
                fees = fees.plus(asset.getPaidExchangeFees(year));
        }
        return fees;
    }

    private BigMoney getOverall() {
        BigMoney overall = BigMoney.zero(mCurrency);
        for (Asset asset : getAssets().values()) {
            overall = overall.plus(asset.getOverall());

        }
        return overall;
    }


    public String toStringHtml() {
        HtmlReport htmlReport = new HtmlReport();
        //Check if there were parsing errors
        if (mTransactionParser.hasParsingErrors())
            htmlReport.appendErrorHeader();

        //First section will be a complete overview over all years
        htmlReport.appendSection("Overall");
        htmlReport.appendSectionSummary(
                getTradeCount(Asset.Type.STOCK, Transaction.Type.SELL),
                getTradeCount(Asset.Type.OTHER, Transaction.Type.SELL),
                getTradeCount(Asset.Type.STOCK, Transaction.Type.BUY),
                getTradeCount(Asset.Type.OTHER, Transaction.Type.BUY),
                getProfitLoss(Asset.Type.STOCK),
                getProfitLoss(Asset.Type.OTHER),
                getPaidFees(Asset.Type.STOCK),
                getPaidFees(Asset.Type.OTHER),
                getPaidExchangeFees(Asset.Type.STOCK),
                getPaidExchangeFees(Asset.Type.OTHER),
                getOverall()
        );
        //And append assets
        for (Asset asset : new TreeSet<>(mAssets.values())) {
            htmlReport.appendAsset(asset);
        }

        //After generate sections for each year
        for (int i = mMaxYear; i >= mMinYear; i--) {
            htmlReport.appendSection(String.valueOf(i));
            htmlReport.appendSectionSummary(
                    getTradeCount(Asset.Type.STOCK, Transaction.Type.SELL, i),
                    getTradeCount(Asset.Type.OTHER, Transaction.Type.SELL, i),
                    getTradeCount(Asset.Type.STOCK, Transaction.Type.BUY, i),
                    getTradeCount(Asset.Type.OTHER, Transaction.Type.BUY, i),
                    getProfitLoss(Asset.Type.STOCK, i),
                    getProfitLoss(Asset.Type.OTHER, i),
                    getPaidFees(Asset.Type.STOCK, i),
                    getPaidFees(Asset.Type.OTHER, i),
                    getPaidExchangeFees(Asset.Type.STOCK, i),
                    getPaidExchangeFees(Asset.Type.OTHER, i),
                    BigMoney.zero(mCurrency)
            );
            for (Asset asset : new TreeSet<>(mAssets.values())) {
                if (asset.getTransactions(i) != null && asset.getTransactions(i).size() > 0) {
                    htmlReport.appendAsset(asset, i);
                }
            }

        }

        //If there were errors append them now
        if (mTransactionParser.hasParsingErrors())
            for (ParsingError error : mTransactionParser.getParsingErrors())
                htmlReport.appendError(error.getErrorMessage());


        return htmlReport.toString();
    }


}
