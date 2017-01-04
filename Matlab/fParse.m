function [no, name, bvdid, ctry, nace, oprev, ta, mcap, emp] = fParse(no, name, bvdid, ctry, nace, oprev, ta, mcap, emp, offset)

size = length(no);

%% Remove header and double quotes
name = strrep(name,'"','');
name = name(offset:size);
bvdid = strrep(bvdid,'"','');
bvdid = bvdid(offset:size);
ctry = strrep(ctry,'"','');
ctry = ctry(offset:size);

%% Format numerical arrays
nace = strrep(nace,'"','');
nace = strrep(nace,',','');
nace = cellfun(@str2num,nace(offset:size)); % Yeah, Matlab can be terrible: "nace" is a cell with strings, but we need it as a numerical array
oprev = strrep(oprev,'"','');
oprev = strrep(oprev,',','');
oprev = cellfun(@str2num,oprev(offset:size));
ta = strrep(ta,'"','');
ta = strrep(ta,',','');
ta = cellfun(@str2num,ta(offset:size));
mcap = strrep(mcap,'"','');
mcap = strrep(mcap,',','');
mcap = cellfun(@str2num,mcap(offset:size));
emp = strrep(emp,'"','');
emp = strrep(emp,',','');
emp = cellfun(@str2num,emp(offset:size));
