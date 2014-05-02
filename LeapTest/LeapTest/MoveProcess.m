% Process the output data of Leap C++ project.
% LI ZHEN, March 17th, 2014.

% fid = fopen('toolResult1.csv', 'w');
% fprintf(fid, 'id, mean, totalTime(us)\n');

for i = 10:12
    filename = sprintf('FingerMove%d.csv', i);
    % time id1 x1 y1 z1 id2 x2 y2 z2 id3 x3 y3 z3
    mat = csvread(filename, 1, 0);
%     figure;
%     scatter3(mat(:, 3), mat(:, 4), mat(:, 5), '.');
%     title(filename);
    
    fprintf('%d:\n', i);
    [row col] = size(mat);
    time = zeros(row, 1);
    
    % ignore the first and last several time stamp
    for r = 5:(row - 5)
        time(r, 1) = mat(r, 6) - mat(r - 1, 6);
        % fprintf('dt:%d\n', time(r, 1));
    end
    tmp_mean = mean(time(5:(row - 5), 1));
    threshold = 0.9;
    time = time .* (time <= tmp_mean * (1 + threshold)) .* (time >= tmp_mean * (1 - threshold));
    % time
    m_mean = sum(time) / sum(time > 0);
    fprintf('mean:%f, time(us):%d\n\n', m_mean, mat(row, 6) - mat(1, 6));
    fprintf(fid, '%d, %f, %d\n', i, m_mean, mat(row, 6) - mat(1, 6));
end

fclose(fid);